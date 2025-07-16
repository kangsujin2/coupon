package com.my.coupon.controller;

import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public void issue(@RequestBody CouponIssueRequestDto requestDto) {
        couponService.issue(requestDto);
    }

}
