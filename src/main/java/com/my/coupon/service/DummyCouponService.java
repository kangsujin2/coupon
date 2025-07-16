package com.my.coupon.service;

import com.my.coupon.dto.CouponIssueRequestDto;
import org.springframework.stereotype.Service;

@Service
public class DummyCouponService implements CouponService{

    @Override
    public void issue(CouponIssueRequestDto requestDto) {
        throw new UnsupportedOperationException("CouponService is not implemented in main branch. Please switch to a strategy branch.");
    }
}
