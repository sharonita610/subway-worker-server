package com.game.subwayworkerserver.controller.request;

import com.game.subwayworkerserver.profanity.ProfanityCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
        @Size(min = 2, max = 20, message = "사용자 이름은 2자 이상 20자 이하이어야 합니다.")
        @ProfanityCheck
        String username,

        @NotBlank(message = "친구 속성은 필수 입력 항목입니다.")
        String friendAttribute,

        @NotNull(message = "성별은 필수 입력 항목입니다.")
        String gender
) {
}
