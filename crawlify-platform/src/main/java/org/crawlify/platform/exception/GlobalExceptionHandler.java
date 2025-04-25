package org.crawlify.platform.exception;

import org.crawlify.common.entity.result.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获所有未处理的异常
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception ex) {
        // 记录日志（可根据需求替换为实际的日志框架）
        return R.fail("系统异常：" + ex.getMessage());
    }

    // 捕获特定的业务异常（可根据需求扩展）
    @ExceptionHandler(RuntimeException.class)
    public R<String> handleRuntimeException(RuntimeException ex) {
        // 记录日志（可根据需求替换为实际的日志框架）
        ex.printStackTrace();
        return R.fail("运行时异常：" + ex.getMessage());
    }
}