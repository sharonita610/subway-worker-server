package com.game.subwayworkerserver.profanity;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProfanityValidator.class) // 사용할 검증기 지정
@Target({ElementType.FIELD, ElementType.PARAMETER}) // 필드와 파라미터에 적용 가능
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfanityCheck {

    String message() default "사용자 이름에 부적절한 단어가 포함되어 있습니다."; // 기본 오류 메시지

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}