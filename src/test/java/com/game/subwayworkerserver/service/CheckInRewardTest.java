package com.game.subwayworkerserver.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckInRewardTest {

    @Test
    void _1일차_보상이_올바르게_반환된다() {
        CheckInReward reward = CheckInReward.getRewardByDay(1);
        assertThat(reward).isEqualTo(CheckInReward.DAY_1);
        assertThat(reward.getCoin()).isEqualTo(30000L);
    }

    @Test
    void _7일차_보상이_올바르게_반환된다() {
        CheckInReward reward = CheckInReward.getRewardByDay(7);
        assertThat(reward).isEqualTo(CheckInReward.DAY_7);
        assertThat(reward.getVacationTicket()).isEqualTo(100L);
    }

    @Test
    void _8일차는_1일차로_순환된다() {
        // (8-1) % 7 + 1 = 1
        CheckInReward reward = CheckInReward.getRewardByDay(8);
        assertThat(reward).isEqualTo(CheckInReward.DAY_1);
    }

    @Test
    void _14일차는_7일차로_순환된다() {
        // (14-1) % 7 + 1 = 7
        CheckInReward reward = CheckInReward.getRewardByDay(14);
        assertThat(reward).isEqualTo(CheckInReward.DAY_7);
    }

    @Test
    void _0이하_입력은_1일차로_처리된다() {
        assertThat(CheckInReward.getRewardByDay(0)).isEqualTo(CheckInReward.DAY_1);
        assertThat(CheckInReward.getRewardByDay(-5)).isEqualTo(CheckInReward.DAY_1);
    }
}
