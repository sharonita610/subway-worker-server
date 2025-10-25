package com.game.subwayworkerserver.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,  "Invalid Input Value"),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Username is already exist"),
    INVALID_FRIEND_ATTRIBUTE(HttpStatus.BAD_REQUEST, "Invalid friend attribute value"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

    private final HttpStatus status;
    private final String message;
}