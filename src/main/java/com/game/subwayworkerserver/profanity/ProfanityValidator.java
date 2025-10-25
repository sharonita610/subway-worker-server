package com.game.subwayworkerserver.profanity;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ProfanityValidator implements ConstraintValidator<ProfanityCheck, String> {

    // ⚠️ 실제 서비스에서는 데이터베이스나 설정 파일에서 관리해야 합니다.
    private static final List<String> PROFANITY_LIST = Arrays.asList(
            "badword",
            "damn",     // 영어 예시
            "ㅅㅂ",    // 한국어 초성/자음 예시
            "바보",     // 한국어 예시
            "욕설"      // 한국어 예시
    );

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.trim().isEmpty()) {
            return true;
        }

        String lowerCaseUsername = username.toLowerCase(Locale.ROOT);

        for (String profanity : PROFANITY_LIST) {
            if (lowerCaseUsername.contains(profanity.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }

        return true;
    }
}
