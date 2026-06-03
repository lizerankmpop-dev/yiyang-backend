package com.neusoft.config;

import com.neusoft.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 企业级规范：
 * 1. 不同异常类型返回对应 HTTP 状态码和业务码
 * 2. 生产环境不暴露内部错误细节
 * 3. 记录请求路径便于排查
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常（@Valid / @Validated 触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return R.badRequest(message);
    }

    /**
     * 缺少必传参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必传参数: {}", e.getParameterName());
        return R.badRequest("缺少必传参数: " + e.getParameterName());
    }

    /**
     * 文件上传超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public R<String> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限: {}", e.getMessage());
        return R.fail(413, "文件大小超出限制");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return R.badRequest(e.getMessage());
    }

    /**
     * 业务运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("业务异常 [{} {}]: {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
        return R.fail("操作失败，请稍后重试");
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleException(Exception e, HttpServletRequest request) {
        log.error("系统内部错误 [{} {}]", request.getMethod(), request.getRequestURI(), e);
        return R.fail("服务器内部错误，请联系管理员");
    }
}
