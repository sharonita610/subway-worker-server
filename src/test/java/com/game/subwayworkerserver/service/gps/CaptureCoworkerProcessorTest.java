package com.game.subwayworkerserver.service.gps;

import com.game.subwayworkerserver.controller.response.coworker.CaptureResponse;
import com.game.subwayworkerserver.domain.reward.BaseReward;
import com.game.subwayworkerserver.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 리팩터링 전 동작을 고정하는 특성 테스트.
 * 확률 판정은 1만 회 표본으로 기대값 ±3%p 안에 들어오는지 본다 (표준편차의 6배 이상이라 흔들리지 않음).
 */
class CaptureCoworkerProcessorTest {

    private static final int ROLLS = 10_000;
    private final CaptureCoworkerProcessor processor = new CaptureCoworkerProcessor();

    private long successes(String tool, int level) {
        return IntStream.range(0, ROLLS).filter(i -> processor.calculateCaptureSucceed(tool, level)).count();
    }

    private long escapes(int level) {
        return IntStream.range(0, ROLLS).filter(i -> processor.calculateEscape(level).run()).count();
    }

    @Test
    void 포획_도구가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> processor.calculateCaptureSucceed("WOOD_SHOVEL", 1))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 존재하지_않는_도구_이름이면_예외가_발생한다() {
        assertThatThrownBy(() -> processor.calculateCaptureSucceed("NOT_AN_ITEM", 1))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void 확률표에_없는_레벨이면_예외가_발생한다() {
        assertThatThrownBy(() -> processor.calculateCaptureSucceed("BRIEFCASE", 8)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> processor.calculateCaptureSucceed("BRIEFCASE", 0)).isInstanceOf(BaseException.class);
    }

    @Test
    void 확률_0인_도구는_항상_실패한다() {
        assertThat(successes("RANDOM_CAPTURE", 1)).isZero();
    }

    @Test
    void 레벨1_동료를_출장용캐리어로_잡으면_약_90퍼센트_성공한다() {
        assertThat(successes("BUSINESS_CARRIER", 1)).isBetween(8_700L, 9_300L);
    }

    @Test
    void 레벨7_동료를_종이박스로_잡으면_약_4퍼센트_성공한다() {
        // 레벨7 기본 20% × 종이박스 20% = 4%
        assertThat(successes("PAPER_BOX", 7)).isBetween(250L, 550L);
    }

    @Test
    void 레벨1_동료는_절대_도망가지_않는다() {
        assertThat(escapes(1)).isZero();
    }

    @Test
    void 레벨7_동료는_약_20퍼센트_도망간다() {
        assertThat(escapes(7)).isBetween(1_700L, 2_300L);
    }

    @Test
    void 확률표에_없는_레벨은_도망_확률이_0이다() {
        assertThat(escapes(99)).isZero();
    }

    @Test
    void 도주_판정_응답은_실패_보상과_빈_동료_정보를_가진다() {
        CaptureResponse response = processor.calculateEscape(1);

        assertThat(response.success()).isFalse();
        assertThat(response.run()).isFalse();
        assertThat(response.reward()).isEqualTo(BaseReward.getFailedCaptureReward());
        assertThat(response.extraExpReward()).isZero();
        assertThat(response.capturedCoworkerInfo()).isNull();
    }
}
