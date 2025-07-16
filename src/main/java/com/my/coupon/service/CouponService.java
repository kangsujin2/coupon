package com.my.coupon.service;

import com.my.coupon.dto.CouponIssueRequestDto;

public interface CouponService {
    void issue(CouponIssueRequestDto requestDto);
}
