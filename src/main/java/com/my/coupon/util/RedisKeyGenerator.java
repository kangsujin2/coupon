package com.my.coupon.util;

public class RedisKeyGenerator {

    public static String couponIssueSetKey(long couponId) {
        return "coupon:issue:" + couponId;
    }

    public static String couponIssueCountKey(long couponId) {
        return "coupon:issue:" + couponId + ":count";
    }
}
