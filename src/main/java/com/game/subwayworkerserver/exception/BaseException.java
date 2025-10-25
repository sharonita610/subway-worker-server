package com.game.subwayworkerserver.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    // BaseException을 상속받는 모든 사용자 정의 예외는 이 생성자를 사용합니다.
    public BaseException(ErrorCode errorCode) {
        // RuntimeException의 메시지로 ErrorCode의 메시지를 사용
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
