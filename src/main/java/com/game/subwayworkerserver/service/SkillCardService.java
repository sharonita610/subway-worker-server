package com.game.subwayworkerserver.service;

import com.game.subwayworkerserver.controller.request.OpenLuckyGachaRequest;
import com.game.subwayworkerserver.controller.request.OpenSkillCardRequest;
import com.game.subwayworkerserver.controller.request.SkillByUserRequest;
import com.game.subwayworkerserver.controller.request.SkillCardConfigureRequest;
import com.game.subwayworkerserver.controller.response.EarnedSkillCardResponse;
import com.game.subwayworkerserver.controller.response.SkillByCoworkerResponse;
import com.game.subwayworkerserver.controller.response.SkillByUserResponse;
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
import java.util.Objects;

import static com.game.subwayworkerserver.domain.skill.SkillCardType.ACTIVE;
import static com.game.subwayworkerserver.domain.skill.SkillCardType.PASSIVE;

@Service
@RequiredArgsConstructor
public class SkillCardService {

    private final UserRepository userRepository;
    private final CoworkerRepository coworkerRepository;
    private final SkillCardRepository skillCardRepository;
    private final SkillCardListFilter skillCardListFilter;

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
}
