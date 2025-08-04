package com.my.coupon.exception;

public enum CouponErrorCode {

    INVALID_COUPON_ISSUE_QUANTITY("Invalid coupon quantity."),
    INVALID_COUPON_ISSUE_DATE("Invalid coupon issuance period."),
    COUPON_NOT_EXIST("Coupon does not exist."),
    DUPLICATED_COUPON_ISSUE("Coupon already issued."),
    FAIL_COUPON_ISSUE_REQUEST("Failed to issue the coupon.")
    ;

    private final String message;

    CouponErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
