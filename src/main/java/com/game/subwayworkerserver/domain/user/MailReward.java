package com.game.subwayworkerserver.domain.user;

import com.game.subwayworkerserver.entity.MailEntity;
import com.game.subwayworkerserver.entity.RewardEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MailReward(
        long mailId,
        String title,
        String sender,
        String content,
        boolean isRead,
        boolean isClaimed,
        List<RewardBaseInfo> rewardList
) {
    public static List<MailReward> toDomain(final List<MailEntity> mailList, final List<RewardEntity> allByMailIdIn) {
        Map<Long, List<RewardEntity>> rewardsByMailId = allByMailIdIn.stream()
                .collect(Collectors.groupingBy(RewardEntity::getMailId));

        return mailList.stream()
                .sorted(Comparator.comparing(MailEntity::isClaimed))
                .map(mail -> {
                    List<RewardEntity> rewards = rewardsByMailId.getOrDefault(mail.getId(), List.of());

                    List<RewardBaseInfo> rewardBaseInfos = rewards.stream()
                            .map(RewardBaseInfo::of)
                            .toList();

                    return new MailReward(
                            mail.getId(),
                            mail.getTitle(),
                            mail.getSender(),
                            mail.getContent(),
                            mail.isRead(),
                            mail.isClaimed(),
                            rewardBaseInfos
                    );
                })
                .toList();
    }

    private record RewardBaseInfo(
            String reward,
            int quantity
    ) {
        public static RewardBaseInfo of(final RewardEntity rewardEntity) {
            return new RewardBaseInfo(rewardEntity.getType().name(), (int) rewardEntity.getQuantity());
        }
    }
}
