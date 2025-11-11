package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.controller.request.UpgradeRequest;
import com.game.subwayworkerserver.controller.response.CoworkerTypesResponse;
import com.game.subwayworkerserver.controller.response.StageCoworkerListResponse;
import com.game.subwayworkerserver.controller.response.UserUpgradeResponse;
import com.game.subwayworkerserver.domain.baseinfo.CoworkerType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.repository.UserGachaRepository;
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
    private final SkillCardRepository skillCardRepository;
    private final UserGachaRepository gachaRepository;

    private static final String ENERZ = "ENERZ";

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
        user.checkAndPurchase(request.price());

        CoworkerEntity target = coworkerRepository.findById(request.targetId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "대상이 존재하지 않습니다."));

        List<CoworkerEntity> materials = getMaterialEntityList(request.materialIds(), user.getId());

        long totalGainedExp = calculateGainedExperience(target, materials);

        coworkerRepository.deleteAll(materials);
        target.accumulateExp(totalGainedExp);
        CoworkerEntity upgraded = coworkerRepository.save(target);
        List<SkillCardEntity> coworkerSkills = skillCardRepository.findAllByCoworkerIdAndInUseTrue(upgraded.getId());

        return UserUpgradeResponse.of(upgraded, coworkerSkills);
    }

    private List<CoworkerEntity> getMaterialEntityList(List<Long> materialIds, long userId) {
        List<CoworkerEntity> materials = coworkerRepository.findAllById(materialIds);

        if (materials.size() != materialIds.size()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "제공된 재료 동료 ID 중 유효하지 않은 항목이 있습니다.");
        }

        materials.stream()
                .filter(coworker -> coworker.getUserId() != userId)
                .findAny()
                .ifPresent(coworker -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "사용자 소유가 아닌 재료 동료가 포함되어 있습니다.");
                });

        return materials;
    }


    private long calculateGainedExperience(CoworkerEntity target, List<CoworkerEntity> materials) {

        long totalBaseXP = 0;

        for (CoworkerEntity material : materials) {

            long materialLevelXP = material.getLevel().getBaseXpForMaterial();
            long materialCurrentXP = material.getCurrentExp();
            long baseXP = materialLevelXP + materialCurrentXP;

            double multiplier = 1.0;

            if (material.getCoworkerType().name().equals(ENERZ) ||
                    material.getCoworkerType().equals(target.getCoworkerType())) {

                multiplier = EnhanceConstants.EXTRA_MULTIPLIER;
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

    public StageCoworkerListResponse getUsingCoworkers(final String uuid) {
        UserEntity user = getUserByUuid(uuid);
        List<CoworkerEntity> coworkerEntityList =
                coworkerRepository.findByUserIdAndOnSaleFalseAndAvailableTrueAndInUseTrue(user.getId());
        List<UserGachaEntity> treasureList = gachaRepository.findAllByUserId(user.getId());
        List<SkillCardEntity> skillCardList = skillCardRepository.findAllByUserIdAndInUseTrueAndAvailableTrue(user.getId());
        return StageCoworkerListResponse.of(coworkerEntityList, treasureList, skillCardList);
    }
}
