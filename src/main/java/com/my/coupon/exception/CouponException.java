package com.my.coupon.exception;

import lombok.Getter;

@Getter
public class CouponException extends RuntimeException{
    private final CouponErrorCode couponErrorCode;

    public CouponException(CouponErrorCode couponErrorCode) {
        super(couponErrorCode.getMessage());
        this.couponErrorCode = couponErrorCode;
    }
}
