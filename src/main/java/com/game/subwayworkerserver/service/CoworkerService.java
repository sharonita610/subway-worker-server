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

        // 1. 대상 동료와 재료 동료 불러오기 및 유효성 검사
        CoworkerEntity target = coworkerRepository.findById(request.targetId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "대상이 존재하지 않습니다."));

        List<CoworkerEntity> materials = coworkerRepository.findAllById(request.materialIds());

        if (materials.size() != request.materialIds().size()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "제공된 재료 동료 ID 중 유효하지 않은 항목이 있습니다.");
        }

        // 권한 검사 (모든 동료가 userId에 속하는지 확인)
        materials.stream()
                .filter(coworker -> coworker.getUserId() != user.getId())
                .findAny()
                .ifPresent(m -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "사용자 소유가 아닌 재료 동료가 포함되어 있습니다.");
                });

        // 2. 경험치 계산
        long totalGainedExp = calculateGainedExperience(target, materials);

        // 3. 경험치 적용 및 레벨업 처리 (핵심 비즈니스 로직)
        target.accumulateExp(totalGainedExp);

        // 4. 재료 동료 제거
        coworkerRepository.deleteAll(materials);

        // 5. 대상 동료 저장 및 반환
        CoworkerEntity upgraded = coworkerRepository.save(target);

        return UserUpgradeResponse.of(upgraded);
    }


    private long calculateGainedExperience(CoworkerEntity target, List<CoworkerEntity> materials) {
        long totalBaseXP = 0;
        boolean hasSameCoworker = false;

        for (CoworkerEntity material : materials) {

            // 1. 재료 동료의 기본 경험치 획득 (CoworkerType 또는 Level에서 가져옴)
            // *가정: CoworkerEntity 내부에 현재 레벨에 따른 기본 XP를 반환하는 메서드가 존재합니다.
            long baseXP = material.getTotalExp();

            double multiplier = 1.0;

            // 2. ENERZ 또는 동일 캐릭터 부스트 적용
            if (material.getCoworkerType().name().equals("ENERZ")) {
                multiplier = EnhanceConstants.ENERZ_XP_MULTIPLIER;
            } else if (material.getCoworkerType().equals(target.getCoworkerType())) {
                multiplier = EnhanceConstants.ENERZ_XP_MULTIPLIER;
            }

            totalBaseXP += (long) (baseXP * multiplier);
        }

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
