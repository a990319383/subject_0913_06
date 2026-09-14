package com.evops.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        String message = error == null ? "请求参数不合法" : error.getField() + " " + error.getDefaultMessage();
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKey(DuplicateKeyException ex) {
        return ApiResponse.fail("关键业务键重复，违反唯一约束");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception ex) {
        return ApiResponse.fail("系统处理失败");
    }
}
