package com.game.subwayworkerserver.controller.request.system;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SystemMailRequest(

        @NotBlank(message = "title은 필수 입력 항목입니다.")
        String title,

        @NotBlank(message = "content는 필수 입력 항목입니다.")
        String content,

        List<SystemRewardRequest> rewards
) {

    public record SystemRewardRequest (
            String rewardName,
            int quantity
    ){
    }
}
