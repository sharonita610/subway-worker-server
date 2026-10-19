package com.game.subwayworkerserver.exception;

import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * @Valid 또는 @Validated 유효성 검사 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, WebRequest request) {

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String errorMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();

        // ErrorCode의 메시지 대신 @Valid의 상세 메시지 사용
        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().getReasonPhrase())
                .message(errorMessage)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 애플리케이션에서 정의한 BaseException 및 이를 상속받는 모든 예외 처리
     */
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ErrorResponse> handleBaseException(BaseException e, WebRequest request) {

        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().getReasonPhrase())
                .message(e.getMessage()) // BaseException에 설정된 메시지 사용
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 서버에서 예상치 못한 모든 예외 처리 (최후의 방어선)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e, WebRequest request) {

        log.error("Unhandled exception on {}: {}", ((ServletWebRequest) request).getRequest().getRequestURI(), e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("서버에서 예상치 못한 에러가 발생했습니다.")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DataIntegrityViolationException (데이터 무결성 위반: Not Null, Unique 제약조건 위반 등) 처리
     * Hibernate/JPA 레벨에서 발생하는 DB 제약 조건 위반 오류입니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e, WebRequest request) {

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        String detailMessage = "데이터베이스 제약 조건(Not Null, Unique 등)을 위반했습니다. 입력 값을 확인해주세요.";
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().getReasonPhrase())
                .message(detailMessage)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {

        // 에러 메시지 추출 (ex. 'verifyUser.name: 사용자 이름에 부적절한 단어가 포함되어 있습니다.')
        String detailedMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(detailedMessage)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /**
     * 지원하지 않는 HTTP Method로 요청 시 발생하는 예외 처리 (HTTP 405 Method Not Allowed)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, WebRequest request) {

        // 405 Method Not Allowed 상태 사용
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        String detailMessage = e.getMessage() != null ?
                e.getMessage() :
                "지원하지 않는 HTTP 요청 방식입니다.";

        // Note: ErrorCode에 405가 명시적으로 정의되어 있다면 그것을 사용해도 됩니다.
        // 여기서는 HttpStatus를 직접 사용하여 처리합니다.

        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(detailMessage)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(response, status);
    }
}
