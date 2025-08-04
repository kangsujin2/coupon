package com.my.coupon.domain;

import com.my.coupon.exception.CouponErrorCode;
import com.my.coupon.exception.CouponException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponIssueValidationResult {
    SUCCESS(1),
    DUPLICATE_REQUEST(-1),
    SOLD_OUT(-2);

    private final int code;

    public static CouponIssueValidationResult fromCode(long code) {
        for (CouponIssueValidationResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    public void handleOrThrow() {
        switch (this) {
            case SUCCESS -> {}
            case DUPLICATE_REQUEST -> throw new CouponException(CouponErrorCode.DUPLICATED_COUPON_ISSUE);
            case SOLD_OUT -> throw new CouponException(CouponErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
            default -> throw new CouponException(CouponErrorCode.FAIL_COUPON_ISSUE_REQUEST);
        }
    }


}
