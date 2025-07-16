package com.my.coupon.entity;

import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String code;

    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    @Enumerated(value = EnumType.STRING)
    private DiscountType discountType;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer totalQuantity;

    private int issuedQuantity;

    private BigDecimal discountValue;

    private String description;

    public boolean isQuantityAvailable() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean isDateAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public boolean isCouponAvailable() {
        if (!isQuantityAvailable()) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
        if (!isDateAvailable()) {
            throw new CouponException(ErrorCode.INVALID_COUPON_ISSUE_DATE);
        }
        return true;
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return endDate.isBefore(now) || isQuantityAvailable();
    }

    public void increaseIssuedQuantity() {
        issuedQuantity++;
    }

}
