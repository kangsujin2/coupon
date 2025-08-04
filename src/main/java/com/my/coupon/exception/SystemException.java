package com.my.coupon.exception;

import lombok.Getter;

@Getter
public class SystemException extends RuntimeException {
    private final SystemErrorCode systemErrorCode;

    public SystemException(SystemErrorCode systemErrorCode) {
        super(systemErrorCode.getMessage());
        this.systemErrorCode = systemErrorCode;
    }
}
