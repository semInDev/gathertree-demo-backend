package com.gathertree.demo.global.response.exception.handler;

import com.gathertree.demo.global.response.ApiResult;
import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResult<Object>> handleGeneralException(GeneralException ex) {
        if (ex.getCause() != null) {
            log.error(
                    "ErrorCode={}, message={}",
                    ex.getErrorStatus().getCode(),
                    ex.getMessage(),
                    ex.getCause()   // 👈 원인 출력
            );
        } else {
            log.error(
                    "ErrorCode={}, message={}",
                    ex.getErrorStatus().getCode(),
                    ex.getMessage()
            );
        }

        return ResponseEntity
                .status(ex.getErrorStatus().getHttpStatus())
                .body(ApiResult.onFailure(
                        ex.getErrorStatus(),
                        ex.getData()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(ErrorStatus.BAD_REQUEST.getHttpStatus())
                .body(ApiResult.onFailure(ErrorStatus.BAD_REQUEST, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Object>> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        return ResponseEntity
                .status(ErrorStatus.BAD_REQUEST.getHttpStatus())
                .body(ApiResult.onFailure(ErrorStatus.BAD_REQUEST, errors));
    }
}
