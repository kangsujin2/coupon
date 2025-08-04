package com.my.coupon.dto;

import com.my.coupon.entity.Coupon;
import com.my.coupon.entity.CouponType;
import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.CouponErrorCode;

public record CacheCouponDto(
        Long couponId,
        CouponType couponType,
        Integer totalQuantity,
        boolean isDateAvailable,
        boolean isQuantityAvailable
){

    public CacheCouponDto(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.isDateAvailable(),
                coupon.isQuantityAvailable()
        );
    }

    public void isCouponAvailable() {
        if (!isQuantityAvailable) {
            throw new CouponException(CouponErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
        if (!isDateAvailable) {
            throw new CouponException(CouponErrorCode.INVALID_COUPON_ISSUE_DATE);
        }
    }

}