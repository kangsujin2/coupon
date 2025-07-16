package com.my.coupon.service.validator;

import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.ErrorCode;
import com.my.coupon.infra.redis.CouponRedisTemplate;
import com.my.coupon.util.RedisKeyGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponValidator {

    private final CouponRedisTemplate redisTemplate;

    public void isCouponIssuable(long couponId, long userId, Integer totalQuantity) {
        if (hasUserAlreadyIssuedCoupon(couponId, userId)) {
            throw new CouponException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }
        if (!isQuantityAvailable(totalQuantity, couponId)) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
    }

    private boolean hasUserAlreadyIssuedCoupon(long couponId, long userId) {
        return redisTemplate.sIsMember(RedisKeyGenerator.couponIssueSetKey(couponId), String.valueOf(userId));
    }

    private boolean isQuantityAvailable(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) return true;
        System.out.println("sCard =" + redisTemplate.sCard(RedisKeyGenerator.couponIssueSetKey(couponId)));
        return totalQuantity > redisTemplate.sCard(RedisKeyGenerator.couponIssueSetKey(couponId));
    }

}
