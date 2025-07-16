package com.my.coupon.repository;

import com.my.coupon.entity.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
    boolean existsByUserIdAndCouponId(long userId, long couponId);
}
