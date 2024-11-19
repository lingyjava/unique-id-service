package com.lingyuan.uniqueid.exception;

/**
 * 自定义业务异常。
 *
 * @author LingYuan
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}