package com.my.coupon.exception;

import lombok.Getter;

@Getter
public enum SystemErrorCode {
    REDIS_SCRIPT_EXECUTION_FAILED("Redis script execution failed."); //

    private final String message;

    SystemErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
