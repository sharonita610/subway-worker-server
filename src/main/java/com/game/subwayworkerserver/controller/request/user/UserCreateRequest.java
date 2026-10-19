package com.game.subwayworkerserver.controller.request.user;

import com.game.subwayworkerserver.profanity.ProfanityCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
        @Size(min = 1, max = 10, message = "사용자 이름은 1자 이상 10자 이하이어야 합니다.")
        @ProfanityCheck
        String username,

        @NotBlank(message = "친구이름은 필수 입력 항목입니다.")
        String coworkerName,

        @NotNull(message = "성별은 필수 입력 항목입니다.")
        String gender,

        String email,
        String realName,
        String tossHash,
        String tossUserKey
) {
}
