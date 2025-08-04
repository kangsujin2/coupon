package com.my.coupon.controller;

import com.my.coupon.dto.CouponIssueResponseDto;
import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponException exception) {
        log.warn("Coupon exception: {}", exception.getMessage());
        return new CouponIssueResponseDto(false, exception.getCouponErrorCode().getMessage());
    }

    @ExceptionHandler(SystemException.class)
    public CouponIssueResponseDto systemExceptionHandler(SystemException exception) {
        log.error("System exception: {}", exception.getMessage(), exception);
        return new CouponIssueResponseDto(false, exception.getSystemErrorCode().getMessage());
    }
}
