package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.OpenSkillCardRequest;
import com.game.subwayworkerserver.controller.request.SkillCardConfigureRequest;
import com.game.subwayworkerserver.controller.request.UpgradeRequest;
import com.game.subwayworkerserver.controller.response.*;
import com.game.subwayworkerserver.domain.baseinfo.ActiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.PassiveSkillCard;
import com.game.subwayworkerserver.domain.baseinfo.SkillCardPaymentOption;
import com.game.subwayworkerserver.domain.skill.SkillCardType;
import com.game.subwayworkerserver.entity.CoworkerEntity;
import com.game.subwayworkerserver.entity.CurrencyType;
import com.game.subwayworkerserver.entity.SkillCardEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.CoworkerRepository;
import com.game.subwayworkerserver.repository.SkillCardRepository;
import com.game.subwayworkerserver.service.UpgradeExecutor.UpgradeResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.game.subwayworkerserver.domain.skill.SkillCardType.ACTIVE;
import static com.game.subwayworkerserver.domain.skill.SkillCardType.PASSIVE;

@Service
@RequiredArgsConstructor
public class SkillCardService {

    private final UserFinder userFinder;
    private final CoworkerRepository coworkerRepository;
    private final SkillCardRepository skillCardRepository;
    private final SkillCardListFilter skillCardListFilter;
    private final UpgradeExecutor upgradeExecutor;

    private static final int COST_PER_CARD = 10000;

    public SkillByCoworkerResponse getSkillListByUserAndCoworker(
            final String uuid, final long coworkerId, final String cardType) {
        UserEntity user = userFinder.getByUuid(uuid);

        CoworkerEntity coworkerEntity = getCoworkerEntity(coworkerId, user.getId());

        SkillCardType skillCardType = SkillCardType.valueOf(cardType.toUpperCase());

        List<Integer> requestedSkill = new ArrayList<>();

        if (skillCardType.equals(ACTIVE)) {
            requestedSkill.add(coworkerEntity.getActiveSkill1());
            requestedSkill.add(coworkerEntity.getActiveSkill2());
        } else {
            requestedSkill.add(coworkerEntity.getPassiveSkill1());
            requestedSkill.add(coworkerEntity.getPassiveSkill2());
        }

        List<SkillCardEntity> skillCardEntityList =
                skillCardRepository.findAllByUserIdAndAvailableTrueAndSkillCardEnumIdInAndOnSaleFalse(user.getId(), requestedSkill);
        List<SkillCardEntity> filteredSkills = skillCardListFilter.defaultFilter(skillCardEntityList);
        return SkillByCoworkerResponse.of(filteredSkills, coworkerEntity.getId());
    }

    private CoworkerEntity getCoworkerEntity(final long coworkerId, final long userId) {
        return coworkerRepository.findByIdAndUserId(coworkerId, userId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Coworker not found.")
        );
    }

    public SkillByUserResponse getUserSkillCards(final String uuid) {
        UserEntity user = userFinder.getByUuid(uuid);
        List<SkillCardEntity> activeSkillCards
                = skillCardRepository.findAllByUserIdAndTypeAndAvailableTrueAndOnSaleFalse(user.getId(), ACTIVE);
        List<SkillCardEntity> passiveSkillCards
                = skillCardRepository.findAllByUserIdAndTypeAndAvailableTrueAndOnSaleFalse(user.getId(), PASSIVE);
        List<SkillCardEntity> filteredActiveSkillCards = skillCardListFilter.defaultFilter(activeSkillCards);
        List<SkillCardEntity> filteredPassiveSkillCards = skillCardListFilter.defaultFilter(passiveSkillCards);

        return SkillByUserResponse.of(filteredActiveSkillCards, filteredPassiveSkillCards);
    }

    @Transactional
    public void addSkillCardToCoworker(final String uuid, final SkillCardConfigureRequest request) {
        UserEntity user = userFinder.getByUuid(uuid);
        SkillCardType skillCardType = SkillCardType.valueOf(request.skillCardType().toUpperCase());
        CoworkerEntity coworker = getCoworkerEntity(request.coworkerId(), user.getId());
        coworker.checkUser(user.getId());

        if (request.skillCardId() == 0) {
            removeSkillCardFromCoworker(coworker.getId(), skillCardType);
            return;
        }

        SkillCardEntity newSkillCard = getSkillCardEntity(request.skillCardId());
        if (newSkillCard.isInUse() && !Objects.equals(newSkillCard.getCoworkerId(), coworker.getId())) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "해당 카드는 이미 다른 동료가 사용 중입니다.");
        }
        removeSkillCardFromCoworker(coworker.getId(), skillCardType);
        coworker.checkSkillAvailability(newSkillCard.getSkillCardEnumId());
        newSkillCard.activateCoworkerId(coworker.getId());
    }

    private void removeSkillCardFromCoworker(long coworkerId, SkillCardType type) {
        skillCardRepository.findByCoworkerIdAndTypeAndInUseTrue(coworkerId, type)
                .ifPresent(SkillCardEntity::deactivateCoworkerId);
    }

    private SkillCardEntity getSkillCardEntity(final long skillCardId) {
        return skillCardRepository.findById(skillCardId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Skill card not found.")
        );
    }

    @Transactional
    public EarnedSkillCardResponse getRandomSkillCardByType(final String uuid, final OpenSkillCardRequest request) {
        UserEntity user = userFinder.getByUuid(uuid);
        CurrencyType currencyType = CurrencyType.confirm(request.payCondition());
        SkillCardType skillCardType = SkillCardType.valueOf(request.skillCardType().toUpperCase());

        SkillCardPaymentOption skillCardPaymentOption =
                SkillCardPaymentOption.confirmCountAndPriceAndCardType(currencyType, request.price(), skillCardType);

        user.purchaseWithVacationTickets(skillCardPaymentOption.getPrice());

        int count = skillCardPaymentOption.getCount();
        List<SkillCardEntity> skillCardEntityList = List.of();

        if (skillCardPaymentOption.getCardType().equals(ACTIVE)) {

            List<ActiveSkillCard> randomCardList = ActiveSkillCard.getRandomCard(count);
            skillCardEntityList = saveActiveSkillCards(user.getId(), randomCardList);
        } else if (skillCardPaymentOption.getCardType().equals(PASSIVE)) {

            List<PassiveSkillCard> randomCardList = PassiveSkillCard.getRandomCard(count);
            skillCardEntityList = savePassiveSkillCards(user.getId(), randomCardList);

        }
        skillCardRepository.saveAll(skillCardEntityList);
        return EarnedSkillCardResponse.of(skillCardEntityList);
    }

    private List<SkillCardEntity> saveActiveSkillCards(final long userId, List<ActiveSkillCard> activeSkillCards) {
        return activeSkillCards.stream()
                .map(activeSkillCard -> SkillCardEntity.of(userId, activeSkillCard))
                .toList();
    }

    private List<SkillCardEntity> savePassiveSkillCards(final long userId, List<PassiveSkillCard> passiveSkillCards) {
        return passiveSkillCards.stream()
                .map(passiveSkillCard -> SkillCardEntity.of(userId, passiveSkillCard))
                .toList();
    }

    public SkillListBySkillIdResponse getSkillsAvailableToUpgrade(final String uuid, final long skillCardId) {
        UserEntity user = userFinder.getByUuid(uuid);
        SkillCardEntity skillCard = skillCardRepository.findById(skillCardId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Skill card not found.")
        );
        int skillCardEnumId = skillCard.getSkillCardEnumId();
        List<SkillCardEntity> skillCardList =
                skillCardRepository.findAllByUserIdAndSkillCardEnumIdAndAvailableTrueAndOnSaleFalseAndIdNot(
                        user.getId(), skillCardEnumId, skillCardId
                );

        return SkillListBySkillIdResponse.of(skillCardList);

    }

    @Transactional
    public UpgradedSkillCardResponse upgradeCard(String uuid, UpgradeRequest request) {

        UserEntity user = userFinder.getByUuid(uuid);

        SkillCardEntity targetCard = getSkillCardEntity(request.targetId());
        targetCard.isMaxLevel();

        List<SkillCardEntity> materialCards = checkMaterialCardIds(
                request.materialIds(), user.getId(), targetCard.getSkillCardEnumId()
        );

        UpgradeResult result = upgradeExecutor.executeSkillCardLogic(targetCard, materialCards);
        targetCard.updateFinalState(result.finalLevel(), result.finalCount(), result.accumulateCount());
        final int totalCost = getTotalCost(result.addedMaterialValue(), request.price());
        user.checkAndPurchase(totalCost);

        skillCardRepository.deleteAll(materialCards);
        SkillCardEntity saved = skillCardRepository.save(targetCard);
        return UpgradedSkillCardResponse.of(saved);
    }

    private static int getTotalCost(final int addCardCount, final int requestPrice) {
        final int totalCost = addCardCount * COST_PER_CARD;
        if (totalCost == requestPrice) {
            return totalCost;
        }
        throw new BaseException(ErrorCode.BAD_REQUEST, "Total cost is wrong.");
    }

    private List<SkillCardEntity> checkMaterialCardIds(
            final List<Long> materialIds, final long userId, final int targetSkillCardId
    ) {

        List<SkillCardEntity> materialCards = skillCardRepository.findAllByIdIn(materialIds);
        if (materialCards.size() != materialIds.size()) {
            throw new BaseException(ErrorCode.NOT_FOUND, "One or more material cards not found.");
        }

        materialCards.stream()
                .filter(card ->
                        !Objects.equals(card.getUserId(), userId) || !card.isAvailable() || card.isInUse()
                )
                .findAny()
                .ifPresent(card -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "Invalid material card status or ownership.");
                });

        materialCards.stream()
                .filter(card -> card.getSkillCardEnumId() != targetSkillCardId)
                .findAny()
                .ifPresent(c -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "Skill cards can only be upgraded with the same card type.");
                });

        return materialCards;
    }

}
