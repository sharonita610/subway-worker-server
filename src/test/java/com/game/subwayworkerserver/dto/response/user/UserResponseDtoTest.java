package com.game.subwayworkerserver.dto.response.user;

import com.game.subwayworkerserver.domain.baseinfo.SubscriptionItem;
import com.game.subwayworkerserver.domain.level.UserLevelConfig;
import com.game.subwayworkerserver.domain.reward.RewardSource;
import com.game.subwayworkerserver.domain.reward.RewardType;
import com.game.subwayworkerserver.domain.user.MailReward;
import com.game.subwayworkerserver.domain.user.UserCheckInReward;
import com.game.subwayworkerserver.domain.user.UserSubscription;
import com.game.subwayworkerserver.dto.response.common.RewardInfo;
import com.game.subwayworkerserver.dto.response.subscription.SubscribeListResponse;
import com.game.subwayworkerserver.entity.RewardEntity;
import com.game.subwayworkerserver.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.game.subwayworkerserver.dto.DtoFixtures.coworker;
import static com.game.subwayworkerserver.dto.DtoFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

class UserResponseDtoTest {

    @Test
    void 사용자_기본_정보는_성별을_소문자로_배지는_이름_목록으로_낸다() {
        UserEntity user = user();

        UserBaseInfo info = UserBaseInfo.of(user, List.of(SubscriptionItem.SILVER_BADGE));

        assertThat(info.id()).isEqualTo(9L);
        assertThat(info.username()).isEqualTo("tester");
        assertThat(info.gender()).isEqualTo("female");
        assertThat(info.coin()).isEqualTo(1200);
        assertThat(info.stageLevel()).isEqualTo(4);
        assertThat(info.vacationTicket()).isEqualTo(7);
        assertThat(info.gachaTicket()).isEqualTo(2);
        assertThat(info.hasPendingReward()).isFalse();
        assertThat(info.hasTrappedSomething()).isFalse();
        assertThat(info.consecutiveAttendanceDays()).isZero();
        assertThat(info.freeAdsUser()).isFalse();
        assertThat(info.badgeName()).containsExactly("SILVER_BADGE");
    }

    @Test
    void 트랩_성공_플래그는_hasTrappedSomething으로_나간다() {
        UserEntity user = user();
        user.updateTrapSuccess();

        assertThat(UserBaseInfo.of(user, List.of()).hasTrappedSomething()).isTrue();
    }

    @Test
    void 사용_중인_동료_정보는_직업_직급_속성을_담는다() {
        List<CoworkerInUseInfo> infos = CoworkerInUseInfo.of(List.of(coworker(10L)));

        assertThat(infos).containsExactly(new CoworkerInUseInfo("MOONGSHIL", "뭉실", "사원", 2, "PERFORMANCE", "성과"));
    }

    @Test
    void 레벨_정보는_레벨표에서_다음_레벨_필요_경험치를_계산한다() {
        UserLevelInfo info = UserLevelInfo.of(user());

        assertThat(info.currentLevel()).isEqualTo(3);
        assertThat(info.currentExp()).isEqualTo(40);
        assertThat(info.totalCumulativeExp()).isEqualTo(640);
        assertThat(info.requiredExpToNextLevel()).isEqualTo(UserLevelConfig.getRequiredExpForLevel(3));
        assertThat(info.stageLevel()).isEqualTo(4);
    }

    @Test
    void 레벨업_보상_응답은_보상_종류와_수량을_옮긴다() {
        RewardType type = RewardType.values()[0];
        RewardEntity reward = RewardEntity.builder()
                .userId(9L).mailId(1L).type(type).source(RewardSource.values()[0]).quantity(5).isClaimed(false).build();

        LevelUpRewardResponse response = LevelUpRewardResponse.of(List.of(reward));

        assertThat(response.rewardList()).containsExactly(RewardInfo.of(type.name(), 5));
    }

    @Test
    void 단순_래핑_응답들은_입력을_그대로_담는다() {
        List<MailReward> mails = List.of();
        List<UserCheckInReward> checkIns = List.of();
        List<UserSubscription> subscriptions = List.of();

        assertThat(UuidResponse.empty().uuid()).isEmpty();
        assertThat(UserMailBoxResponse.of(mails).mailList()).isSameAs(mails);
        assertThat(CheckInListResponse.of(checkIns).rewardList()).isSameAs(checkIns);
        assertThat(SubscribeListResponse.of(subscriptions).subscriptionList()).isSameAs(subscriptions);
        UserResponse response = new UserResponse(UserBaseInfo.of(user(), List.of()), UserLevelInfo.of(user()), List.of());
        assertThat(response.baseInfo().username()).isEqualTo("tester");
        assertThat(response.coworkerInUseInfoList()).isEmpty();
    }
}
