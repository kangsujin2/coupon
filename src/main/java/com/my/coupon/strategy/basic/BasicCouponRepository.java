package com.my.coupon.strategy.basic;

import com.my.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("basicCouponRepo")
public interface BasicCouponRepository extends JpaRepository<Coupon, Long> {
}
