package com.game.subwayworkerserver.service; // 패키지 변경 가정

import com.game.subwayworkerserver.domain.StageRewardInfo;
import com.game.subwayworkerserver.domain.StageRewardInfo.ExtraReward;
import com.game.subwayworkerserver.domain.baseinfo.GachaTreasure;
import com.game.subwayworkerserver.domain.baseinfo.StageReward;
import com.game.subwayworkerserver.entity.UserEntity;
import com.game.subwayworkerserver.entity.UserGachaEntity;
import com.game.subwayworkerserver.exception.BaseException;
import com.game.subwayworkerserver.exception.ErrorCode;
import com.game.subwayworkerserver.repository.UserGachaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StageRewardProcessor {

    private final UserGachaRepository gachaRepository;

    private static final int TREASURE_COUNT = 1;
    private static final int MAX_TREASURE_COUNT = 20;
    private static final String GACHA_TICKET = "GACHA_TICKET";

    public StageRewardInfo giveFirstClearReward(UserEntity user, int requestedLevel) {
        StageReward reward = getStageReward(requestedLevel);

        return StageRewardInfo.builder()
                .userExp(reward.getPersonalExp())
                .coworkerExp(reward.getPartnerExp())
                .coin(reward.getFirstCoin())
                .gachaTicket(reward.getGachaTicketCount())
                .vacationTicket(reward.getVacationTicketCount())
                .build();
    }

    private static StageReward getStageReward(int requestedLevel) {
        return StageReward.getRewardByLevel(requestedLevel)
                .orElseThrow(() -> new IllegalArgumentException("Invalid stage level: " + requestedLevel));
    }

    public StageRewardInfo giveRepeatClearReward(UserEntity user, int requestedLevel) {
        StageReward reward = getStageReward(requestedLevel);
        String additionalReward = processAdditionalReward(user, reward.getTreasureDropRate(), reward.getGachaTicketDropRate());

        ExtraReward extraReward = null;
        if (additionalReward != null && !additionalReward.isEmpty()) {
            extraReward = new ExtraReward(additionalReward, TREASURE_COUNT);
        } else {
            extraReward = new ExtraReward("", 0);
        }

        return StageRewardInfo.builder()
                .userExp(reward.getRePersonalExp())
                .coworkerExp(reward.getRePartnerExp())
                .coin(reward.getReCoin())
                .extraReward(extraReward)
                .build();

    }

    private String processAdditionalReward(UserEntity user, double treasureDropRate, double gachaTicketDropRate) {
        double randomValue = ThreadLocalRandom.current().nextDouble();

        double totalCombinedRate = treasureDropRate + gachaTicketDropRate;

        if (randomValue < totalCombinedRate) {

            if (randomValue < treasureDropRate) {
                // 보물 획득 (PT 확률)

                GachaTreasure randomTreasure = getRandomTreasure(user.getId());
                return randomTreasure.name();

            } else {
                return GACHA_TICKET;
            }
        }

        return "";
    }

    private GachaTreasure getRandomTreasure(final long userId) {
        Map<GachaTreasure, Integer> userTreasureCounts = getUserTreasureCounts(userId);
        List<GachaTreasure> allTreasures = List.of(GachaTreasure.values());

        List<GachaTreasure> maxedOutTreasures = allTreasures.stream()
                .filter(treasure -> userTreasureCounts.getOrDefault(treasure, 0) == MAX_TREASURE_COUNT)
                .toList();

        if (maxedOutTreasures.size() == MAX_TREASURE_COUNT) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "모든 보물의 최대 보유 한도(20개)에 도달했습니다.");
        }

        return GachaTreasure.getRandom(maxedOutTreasures);
    }

    private Map<GachaTreasure, Integer> getUserTreasureCounts(final long userId) {
        List<UserGachaEntity> userGachaRecords = gachaRepository.findAllByUserId(userId);
        return userGachaRecords.stream()
                .collect(Collectors.toMap(
                        UserGachaEntity::getGacha,
                        UserGachaEntity::getGachaCount
                ));
    }
}
