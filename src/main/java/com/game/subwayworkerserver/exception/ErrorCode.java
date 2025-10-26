package com.game.subwayworkerserver.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,  "Invalid input value."),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Username already exists."),
    INVALID_FRIEND_ATTRIBUTE(HttpStatus.BAD_REQUEST, "Invalid friend attribute value."),
    NOT_VALID_ID(HttpStatus.BAD_REQUEST, "Not Valid ID" ),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"Not Found."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error."),
    ;

    private final HttpStatus status;
    private final String message;
}