package com.my.coupon.strategy.basic;

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
public class BasicCouponService implements CouponService {

    private final BasicCouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Override
    public void issue(CouponIssueRequestDto requestDto) {
        long couponId = requestDto.couponId();
        long userId = requestDto.userId();

        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_EXIST));

        hasUserAlreadyIssuedCoupon(couponId, userId);

        if (coupon.isCouponAvailable()) {
            CouponIssue couponIssue = CouponIssue.builder().couponId(couponId).userId(userId).build();
            couponIssueRepository.save(couponIssue);
            coupon.increaseIssuedQuantity();
        }
    }

    private void hasUserAlreadyIssuedCoupon(long couponId, long userId) {
        boolean alreadyIssued = couponIssueRepository.existsByUserIdAndCouponId(userId, couponId);
        if (alreadyIssued) {
            throw new CouponException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }
    }
}
