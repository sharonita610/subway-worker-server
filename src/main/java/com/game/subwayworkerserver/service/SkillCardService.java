package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.OpenSkillCardRequest;
import com.game.subwayworkerserver.controller.request.SkillByUserRequest;
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
import com.game.subwayworkerserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.game.subwayworkerserver.domain.skill.SkillCardType.ACTIVE;
import static com.game.subwayworkerserver.domain.skill.SkillCardType.PASSIVE;
import static com.game.subwayworkerserver.exception.ErrorCode.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class SkillCardService {

    private final UserRepository userRepository;
    private final CoworkerRepository coworkerRepository;
    private final SkillCardRepository skillCardRepository;
    private final SkillCardListFilter skillCardListFilter;

    private static final int COST_PER_CARD = 10000;
    private static final int MAX_LEVEL = 5;

    public SkillByCoworkerResponse getSkillListByUserAndCoworker(String uuid, SkillByUserRequest request) {
        UserEntity user = getUserEntityByUuid(uuid);

        CoworkerEntity coworkerEntity = getCoworkerEntity(request.coworkerId());

        SkillCardType skillCardType = SkillCardType.valueOf(request.cardType().toUpperCase());

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
        return SkillByCoworkerResponse.of(skillCardEntityList, coworkerEntity.getId());
    }

    private CoworkerEntity getCoworkerEntity(final long coworkerId) {
        return coworkerRepository.findById(coworkerId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Coworker not found.")
        );
    }

    private UserEntity getUserEntityByUuid(final String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    public SkillByUserResponse getUserSkillCards(final String uuid) {
        UserEntity user = getUserEntityByUuid(uuid);
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
        UserEntity user = getUserEntityByUuid(uuid);
        SkillCardType skillCardType = SkillCardType.valueOf(request.skillCardType().toUpperCase());
        CoworkerEntity coworker = getCoworkerEntity(request.coworkerId());
        coworker.checkUser(user.getId());

        if (request.skillCardId() == 0) {
            removeSkillCardFromCoworker(user.getId(), skillCardType);
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
        UserEntity user = getUserEntityByUuid(uuid);
        CurrencyType currencyType = CurrencyType.confirm(request.payCondition());
        SkillCardType skillCardType = SkillCardType.valueOf(request.skillCardType().toUpperCase());

        SkillCardPaymentOption skillCardPaymentOption =
                SkillCardPaymentOption.confirmCountAndPriceAndCardType(currencyType, request.price(), skillCardType);
        checkUserHasEnoughTicketsToPay(request.price(), user, currencyType);

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


    private void checkUserHasEnoughTicketsToPay(
            final int price, final UserEntity user, final CurrencyType currencyType
    ) {
        boolean enoughTickets = user.hasEnoughTickets(currencyType.getCurrencyType(), price);
        if (!enoughTickets) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "Not enough tickets.");
        }
    }

    public SkillListBySkillIdResponse getSkillsAvailableToUpgrade(final String uuid, final long skillCardId) {
        UserEntity user = getUserEntityByUuid(uuid);
        SkillCardEntity skillCard = skillCardRepository.findById(skillCardId).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Skill card not found.")
        );
        int skillCardEnumId = skillCard.getSkillCardEnumId();
        List<SkillCardEntity> skillCardList =
                skillCardRepository.findAllByUserIdAndSkillCardEnumIdAndAvailableTrueAndOnSaleFalse(user.getId(), skillCardEnumId);

        return SkillListBySkillIdResponse.of(skillCardList);

    }

    @Transactional
    public UpgradedSkillCardResponse upgradeCard(String uuid, UpgradeRequest request) {

        UserEntity user = getUserByUuid(uuid);

        // 1. 대상 카드 조회
        SkillCardEntity targetCard = skillCardRepository.findById(request.targetId()).orElseThrow(
                () -> new BaseException(ErrorCode.NOT_FOUND, "Target card not found.")
        );
        targetCard.isMaxLevel();

        // 2. 재료 카드 유효성 검사 및 리스트 획득
        List<SkillCardEntity> materialCards = checkMaterialCardIds(request, user, targetCard);

        final int addCardCount = materialCards.size();
        final long totalCost = (long) addCardCount * COST_PER_CARD; // long으로 캐스팅하여 오버플로우 방지

        // 3. 코인 검사 및 차감
        // request.price() 대신 totalCost를 사용해야 합니다. (request.price()가 totalCost와 동일해야 함)
        checkUserCoin(user, (int)totalCost); // 코인 보유량 확인
        user.purchaseWithCoin((int)totalCost); // 코인 차감

        // 4. 레벨업 계산 (executeUpgradeLogic에서 최종 레벨/카운트를 계산)
        UpgradeResult result = executeUpgradeLogic(targetCard, addCardCount);

        // 5. 대상 카드 최종 상태 업데이트 (DB에 반영될 내용)
        targetCard.updateFinalState(result.finalLevel(), result.finalCount());

        // 6. 재료 카드 제거
        skillCardRepository.deleteAll(materialCards);

        // 7. 결과 반환을 위해 대상 카드 저장 (updateFinalState가 @Transactional 내에서 변경을 추적하지만, 명시적 저장이 필요할 수 있음)
        skillCardRepository.save(targetCard);

        // SkillListBySkillIdResponse DTO로 변환하여 반환한다고 가정
        return UpgradedSkillCardResponse.of(targetCard);
    }

    private UpgradeResult executeUpgradeLogic(SkillCardEntity targetCard, int addCardCount) {

        int currentLevel = targetCard.getCurrentLevel();
        int totalNewCount = targetCard.getCount() + addCardCount;
        while (currentLevel < MAX_LEVEL) {

            int requiredCards = LEVEL_UP_REQUIREMENTS.getOrDefault(currentLevel, MAX_LEVEL + 1);

            if (totalNewCount >= requiredCards) {
                totalNewCount -= requiredCards;
                currentLevel++;

                if (currentLevel >= MAX_LEVEL) {
                    break;
                }

            } else {
                break;
            }
        }

        return new UpgradeResult(currentLevel, totalNewCount);
    }


    private static final Map<Integer, Integer> LEVEL_UP_REQUIREMENTS = Map.of(
            1, 1, // Lv1 -> Lv2에 필요한 추가 카드 수: 1장
            2, 2, // Lv2 -> Lv3에 필요한 추가 카드 수: 2장
            3, 3, // Lv3 -> Lv4에 필요한 추가 카드 수: 3장
            4, 4  // Lv4 -> Lv5에 필요한 추가 카드 수: 4장
    );

    private List<SkillCardEntity>  checkMaterialCardIds(UpgradeRequest request, UserEntity user, SkillCardEntity targetCard) {
        List<SkillCardEntity> materialCards = skillCardRepository.findAllByIdIn(request.materialIds());
        if (materialCards.size() != request.materialIds().size()) {
            throw new BaseException(ErrorCode.NOT_FOUND, "One or more material cards not found.");
        }

        materialCards.stream()
                .filter(c -> !Objects.equals(c.getUserId(), user.getId()) || !c.isAvailable() || c.isInUse())
                .findAny()
                .ifPresent(c -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "Invalid material card status or ownership.");
                });

        // 2-2. 동일 카드 강화 확인 (가장 중요)
        materialCards.stream()
                .filter(c -> c.getSkillCardEnumId() != targetCard.getSkillCardEnumId())
                .findAny()
                .ifPresent(c -> {
                    throw new BaseException(ErrorCode.BAD_REQUEST, "Skill cards can only be upgraded with the same card type.");
                });

        return materialCards;
    }

    private static void checkUserCoin(final UserEntity user, final int price) {
        boolean ableToBuy = user.hasCoinMoreThanItemPrice(price);
        if (!ableToBuy) {
            throw new BaseException(BAD_REQUEST, "코인이 부족해요!");
        }
    }

    private UserEntity getUserByUuid(String uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new BaseException(ErrorCode.USER_NOT_FOUND)
        );
    }

    private record UpgradeResult(int finalLevel, int finalCount) {}

}
