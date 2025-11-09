package com.game.subwayworkerserver.scheduler;

import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.entity.UserSubscriptionEntity;
import com.game.subwayworkerserver.repository.UserSubscriptionRepository;
import com.game.subwayworkerserver.service.RewardProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionSchedulerService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final RewardProcessor rewardProcessor;

    @Scheduled(cron = "0 0 0 * * *")
    public void grantDailySubscriptionRewardsScheduled() {

        LocalDate today = LocalDate.now();

        List<UserSubscriptionEntity> activeSubscriptions =
                userSubscriptionRepository.findAllByExpirationDateGreaterThanEqual(today);

        for (UserSubscriptionEntity sub : activeSubscriptions) {
            SubscriptionItem subscriptionItem = sub.getType();
            String mailTitle = String.format("%s 데일리 보상", subscriptionItem.getKorName());
            rewardProcessor.sendDailySubscriptionReward(
                    sub.getUserId(),
                    mailTitle,
                    subscriptionItem.getDailyTicket()
            );
        }
    }
}