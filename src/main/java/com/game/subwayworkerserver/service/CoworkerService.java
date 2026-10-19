package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.PartnerConfigureRequest;
import com.game.subwayworkerserver.controller.request.UpgradeRequest;
import com.game.subwayworkerserver.controller.response.coworker.CoworkerTypesResponse;
import com.game.subwayworkerserver.controller.response.coworker.StageCoworkerListResponse;
import com.game.subwayworkerserver.controller.response.coworker.UserUpgradeResponse;
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
import com.game.subwayworkerserver.domain.coworker.CoworkerUpgradeVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoworkerService {

    private final CoworkerRepository coworkerRepository;
    private final UserFinder userFinder;
    private final SkillCardRepository skillCardRepository;
    private final UserGachaRepository gachaRepository;
    private final CoworkerUpgradeProcessor coworkerUpgradeProcessor;
    private final CoworkerLevelProcessor coworkerLevelProcessor;

    private static final int UPGRADE_PRICE_PER_COWORKER = 300;

    public CoworkerTypesResponse getCoworkerElements() {
        return CoworkerTypesResponse.from(CoworkerType.getAll());
    }

    @Transactional
    public void configurePartners(final String uuid, final PartnerConfigureRequest request) {

        UserEntity user = userFinder.getByUuid(uuid);
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

        UserEntity user = userFinder.getByUuid(uuid);

        CoworkerEntity target = coworkerRepository.findById(request.targetId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "대상이 존재하지 않습니다."));

        List<CoworkerEntity> materials = getMaterialEntityList(request.materialIds(), user.getId());

        CoworkerUpgradeVO upgradeVO = coworkerUpgradeProcessor.upgradeCoworker(target, materials);
        int upgradePrice = upgradeVO.basePoint() * UPGRADE_PRICE_PER_COWORKER;
        user.checkAndPurchase(upgradePrice);

        coworkerRepository.deleteAll(materials);
        coworkerLevelProcessor.processCoworkerExpAndLevelUp(target, upgradeVO.newExp());
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

    public StageCoworkerListResponse getUsingCoworkers(final String uuid) {
        UserEntity user = userFinder.getByUuid(uuid);
        List<CoworkerEntity> coworkerEntityList =
                coworkerRepository.findByUserIdAndOnSaleFalseAndAvailableTrueAndInUseTrue(user.getId());
        List<UserGachaEntity> treasureList = gachaRepository.findAllByUserId(user.getId());
        List<SkillCardEntity> skillCardList = skillCardRepository.findAllByUserIdAndInUseTrueAndAvailableTrue(user.getId());
        return StageCoworkerListResponse.of(coworkerEntityList, treasureList, skillCardList);
    }
}
