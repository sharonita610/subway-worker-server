package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.controller.request.UpgradeRequest;
import com.game.subwayworkerserver.controller.response.CoworkerTypesResponse;
import com.game.subwayworkerserver.controller.response.UserUpgradeResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class CoworkerService {

    private final CoworkerRepository coworkerRepository;
    private final UserRepository userRepository;

    public CoworkerTypesResponse getCoworkerElements() {
        return CoworkerTypesResponse.from(CoworkerType.getAll());
    }

    @Transactional
    public void configurePartners(final String uuid, final PartnerConfigureRequest request) {

        UserEntity user = getUserByUuid(uuid);
        long userId = user.getId();


        List<Long> requestCoworkerIds = request.coworkerIds();
        if (requestCoworkerIds.isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "파트너는 최소 1명, 최대 3명까지 지정 가능해요");
        }
        List<CoworkerEntity> currentPartners = coworkerRepository.findByUserIdAndInUseTrue(userId);

        currentPartners.stream()
                .filter(p -> !requestCoworkerIds.contains(p.getId()))
                .forEach(CoworkerEntity::setInUseFalse);

        List<CoworkerEntity> requestedCoworkers = coworkerRepository.findAllById(requestCoworkerIds);
        requestedCoworkers.forEach(coworker -> {
            if (coworker.getUserId() == userId) {
                coworker.setInUseTrue();
            } else {
                throw new BaseException(ErrorCode.BAD_REQUEST, "자신의 동료만 파트너로 지정할 수 있습니다.");
            }
        });
    }

    @Transactional
    public UserUpgradeResponse upgradeCoworker(final String uuid, final UpgradeRequest request) {

        UserEntity user = getUserByUuid(uuid);
        checkUserCoin(user, request.price());

        CoworkerEntity target = coworkerRepository.findById(request.targetId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "대상이 존재하지 않습니다."));

        List<CoworkerEntity> materials = getMaterialEntityList(request.materialIds(), user.getId());

        long totalGainedExp = calculateGainedExperience(target, materials);

        target.accumulateExp(totalGainedExp);
        coworkerRepository.deleteAll(materials);
        CoworkerEntity upgraded = coworkerRepository.save(target);
        user.purchaseWithCoin(request.price());
        return UserUpgradeResponse.of(upgraded);
    }

    private static void checkUserCoin(final UserEntity user, final int price) {
        boolean ableToBuy = user.hasCoinMoreThanItemPrice(price);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "코인이 부족해요!");
        }
    }

    private List<CoworkerEntity> getMaterialEntityList(List<Long> materialIds, long userId) {
        List<CoworkerEntity> materials = coworkerRepository.findAllById(materialIds);

        if (materials.size() != materialIds.size()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "제공된 재료 동료 ID 중 유효하지 않은 항목이 있습니다.");
        }

        materials.stream()
                .filter(coworker -> coworker.getUserId() != userId)
                .findAny()
                .ifPresent(m -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "사용자 소유가 아닌 재료 동료가 포함되어 있습니다.");
                });
        return materials;
    }


    private long calculateGainedExperience(CoworkerEntity target, List<CoworkerEntity> materials) {

        long totalBaseXP = 0;

        // 2-1. 재료 동료의 Base XP 합산 및 ENERZ/동일 캐릭터 보너스 적용
        for (CoworkerEntity material : materials) {

            // a) 레벨에 따른 고정 XP 획득 (CoworkerLevel의 getBaseXpForMaterial() 사용 가정)
            long materialLevelXP = material.getLevel().getBaseXpForMaterial();

            // b) 재료 동료가 현재 레벨에서 모은 XP (예시의 200/300 중 200)
            long materialCurrentXP = material.getCurrentExp();

            // c) 총 기본 XP = 레벨 고정 XP + 현재 획득 XP
            long baseXP = materialLevelXP + materialCurrentXP;

            double multiplier = 1.0;

            if (material.getCoworkerType().name().equals("ENERZ") ||
                    material.getCoworkerType().equals(target.getCoworkerType())) {

                multiplier = EnhanceConstants.ENERZ_XP_MULTIPLIER; // 1.2배
            }

            totalBaseXP += (long) (baseXP * multiplier);
        }

        // 4. 확률 보너스 계산 및 최종 경험치 합산
        double critMultiplier = EnhanceConstants.getCriticalMultiplier();
        long critXP = (long) (EnhanceConstants.CRIT_STANDARD_XP * critMultiplier);
        return totalBaseXP + critXP;
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
