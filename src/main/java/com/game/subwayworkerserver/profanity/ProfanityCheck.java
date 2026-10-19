package com.game.subwayworkerserver.profanity;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProfanityValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfanityCheck {

    String message() default "사용자 이름에 부적절한 단어가 포함되어 있습니다."; // 기본 오류 메시지

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}