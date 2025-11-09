package com.game.subwayworkerserver.domain.user;

import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.entity.RewardType;
import com.game.subwayworkerserver.entity.UserSubscriptionEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record UserSubscription(
        int id,
        String korName,
        int price,
        boolean subscribe,
        List<SubscriptionInfo> rewardList
) {

    public static List<UserSubscription> toDomain(
            final List<SubscriptionItem> subscriptionItems, final List<UserSubscriptionEntity> allSubscriptions
    ) {
        LocalDate today = LocalDate.now();

        Map<SubscriptionItem, UserSubscriptionEntity> activeSubscriptionsMap = allSubscriptions.stream()
                .filter(sub -> !sub.getExpirationDate().isBefore(today))
                .collect(Collectors.toMap(
                        UserSubscriptionEntity::getType,
                        sub -> sub,
                        (existing, replacement) -> existing.getExpirationDate().isAfter(replacement.getExpirationDate()) ? existing : replacement
                ));

        return subscriptionItems.stream()
                .map(item -> {
                    UserSubscriptionEntity activeSub = activeSubscriptionsMap.get(item);
                    boolean isSubscribed = activeSub != null;

                    List<SubscriptionInfo> dailyRewards = createDailyRewardList(item);

                    return new UserSubscription(
                            item.getId(),
                            item.getKorName(),
                            item.getPrice(),
                            isSubscribed,
                            dailyRewards
                    );
                })
                .toList();
    }
    private static List<SubscriptionInfo> createDailyRewardList(SubscriptionItem item) {
        List<SubscriptionInfo> list = new ArrayList<>();

        if (item.getDailyTicket() > 0) {
            list.add(new SubscriptionInfo(RewardType.VACATION_TICKET.name(), item.getDailyTicket()));
        }
        if (item.getInstanceTicket() > 0) {
            list.add(new SubscriptionInfo(RewardType.VACATION_TICKET.name(), item.getInstanceTicket()));
        }
        if (item.getAdSkipDays() > 0) {
            list.add(new SubscriptionInfo(RewardType.NO_ADS.name(), item.getAdSkipDays()));
        }

        return list;
    }
    private record SubscriptionInfo(
            String rewardName,
            int quantity
    ) {
    }
}
