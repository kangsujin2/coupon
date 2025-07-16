package com.my.coupon.strategy.optimistic;

import com.my.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("OptimisticCouponRepo")
public interface OptimisticCouponRepository extends JpaRepository<Coupon, Long> {

    @Modifying
    @Query("""
    UPDATE Coupon c
       SET c.issuedQuantity = c.issuedQuantity + 1
     WHERE c.id = :couponId
       AND c.issuedQuantity < c.totalQuantity
    """)
    int increaseIssuedQuantityIfAvailable(@Param("couponId") long couponId);
}
