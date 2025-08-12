package com.my.coupon.exception;

import lombok.Getter;

@Getter
public enum SystemErrorCode {
    REDIS_SCRIPT_EXECUTION_FAILED("Redis script execution failed."), //
    REDIS_ROLLBACK_FAILED("Failed to rollback Redis state"),
    KAFKA_SEND_FAILED("Failed to send Kafka message.")
    ;
    private final String message;

    SystemErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
