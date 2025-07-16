package com.my.coupon.service;

import com.my.coupon.dto.CacheCouponDto;
import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.service.cache.CouponCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@Transactional
@RequiredArgsConstructor
public class RedisCouponService implements CouponService {

    private final CouponCacheService couponCacheService;

    @Override
    public void issue(CouponIssueRequestDto requestDto) {
        long couponId = requestDto.couponId();
        long userId = requestDto.userId();

        CacheCouponDto couponDto = couponCacheService.getCouponLocalCache(couponId);
        couponDto.isCouponAvailable();

        /*
        TODO: Issue logic (distributed lock + Redis queue) will be implemented later.
        Currently, only cache retrieval is handled.
         */

    }
}
