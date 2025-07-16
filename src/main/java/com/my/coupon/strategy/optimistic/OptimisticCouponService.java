package com.my.coupon.strategy.optimistic;

import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.entity.Coupon;
import com.my.coupon.entity.CouponIssue;
import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.ErrorCode;
import com.my.coupon.repository.CouponIssueRepository;
import com.my.coupon.service.CouponService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@Transactional
@RequiredArgsConstructor
public class OptimisticCouponService implements CouponService {
    private final OptimisticCouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Override
    public void issue(CouponIssueRequestDto requestDto) {

        long couponId = requestDto.couponId();
        long userId = requestDto.userId();

        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_EXIST));
        coupon.isCouponAvailable();
        hasUserAlreadyIssuedCoupon(couponId, userId);

        int updated = couponRepository.increaseIssuedQuantityIfAvailable(couponId);
        if (updated == 0) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
        CouponIssue couponIssue = CouponIssue.builder().couponId(couponId).userId(userId).build();
        couponIssueRepository.save(couponIssue);
    }

    private void hasUserAlreadyIssuedCoupon(long couponId, long userId) {
        boolean alreadyIssued = couponIssueRepository.existsByUserIdAndCouponId(userId, couponId);
        if (alreadyIssued) {
            throw new CouponException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }
    }
}
