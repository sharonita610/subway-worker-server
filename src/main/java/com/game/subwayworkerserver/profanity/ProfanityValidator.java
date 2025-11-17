package com.game.subwayworkerserver.profanity;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProfanityValidator implements ConstraintValidator<ProfanityCheck, String> {

    private final ProfanityService profanityService;

    @Override
    public void initialize(ProfanityCheck constraintAnnotation) {
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.trim().isEmpty()) {
            return true;
        }

        String normalizedUsername = username
                .replaceAll("\\s", "") // 띄어쓰기 제거
                .toLowerCase(Locale.ROOT);

        Set<String> profanitySet = profanityService.getProfanitySet();

        for (String profanity : profanitySet) {
            if (normalizedUsername.contains(profanity)) {
                return false;
            }
        }


        return true;
    }
}
