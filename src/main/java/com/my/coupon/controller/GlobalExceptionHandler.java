package com.my.coupon.controller;

import com.my.coupon.dto.CouponIssueResponseDto;
import com.my.coupon.exception.CouponException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CouponException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponException exception) {
        return new CouponIssueResponseDto(false, exception.getErrorCode().getMessage());
    }
}
