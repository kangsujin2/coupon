package com.my.coupon.service.cache;

import com.my.coupon.dto.CacheCouponDto;
import com.my.coupon.entity.Coupon;
import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.CouponErrorCode;
import com.my.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCacheService {

    private final CouponRepository couponRepository;

    @Cacheable(cacheNames = "coupon")
    public CacheCouponDto getCouponCache(long couponId) {
        Coupon coupon =  couponRepository.findById(couponId).orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_EXIST));
        return new CacheCouponDto(coupon);
    }

    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CacheCouponDto getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CacheCouponDto putCouponCache(long couponId) {
        return getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CacheCouponDto putCouponLocalCache(long couponId) {
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        return (CouponCacheService) AopContext.currentProxy();
    }




}