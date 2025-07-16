package com.my.coupon.repository;

import com.my.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("redisCouponRepo")
public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
