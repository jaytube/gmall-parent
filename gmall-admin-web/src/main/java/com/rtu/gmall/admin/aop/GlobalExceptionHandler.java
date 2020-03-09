package com.rtu.gmall.admin.aop;

import com.rtu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ArithmeticException.class)
    public Object arithmeticExHandler() {
        log.error("arithmetic错误");
        return new CommonResult().validateFailed("数学没学好！");
    }

    @ExceptionHandler(NullPointerException.class)
    public Object npeHandler() {
        log.error("null pointer exception ...");
        return new CommonResult().validateFailed("空指针了...");
    }
}
