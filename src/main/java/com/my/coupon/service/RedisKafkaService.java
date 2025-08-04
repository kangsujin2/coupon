package com.my.coupon.service;

import com.my.coupon.domain.CouponIssueValidationResult;
import com.my.coupon.dto.CacheCouponDto;
import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.entity.CouponIssue;
import com.my.coupon.exception.SystemErrorCode;
import com.my.coupon.exception.SystemException;
import com.my.coupon.infra.CouponIssueKafkaProducer;
import com.my.coupon.repository.CouponIssueRepository;
import com.my.coupon.repository.CouponRepository;
import com.my.coupon.service.cache.CouponCacheService;
import com.my.coupon.util.RedisKeyGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisKafkaService implements CouponService {

    private final CouponCacheService couponCacheService;
    private final RedisScript<List> couponIssueValidationScript;
    private final RedisTemplate<String, String> redisTemplate;
    private final CouponIssueKafkaProducer couponIssueKafkaProducer;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;

    @Override
    public void issue(CouponIssueRequestDto requestDto) {
        long couponId = requestDto.couponId();
        long userId = requestDto.userId();

        CacheCouponDto couponDto = couponCacheService.getCouponLocalCache(couponId);
        couponDto.isCouponAvailable();

        validateIssue(couponId, userId, couponDto.totalQuantity());
        couponIssueKafkaProducer.send(requestDto);
    }

    private void validateIssue(long couponId, long userId, int totalQuantity) {
        String countKey = RedisKeyGenerator.couponIssueCountKey(couponId);
        String setKey = RedisKeyGenerator.couponIssueSetKey(couponId);

        List<String> keys = List.of(countKey, setKey);
        List<String> args = List.of(String.valueOf(userId), String.valueOf(totalQuantity));

        List<?> result = redisTemplate.execute(couponIssueValidationScript, keys, args.toArray());

        if (result == null || result.isEmpty()){ throw new SystemException(SystemErrorCode.REDIS_SCRIPT_EXECUTION_FAILED);}

        long statusCode = (long) result.get(0);
        long count = (long) result.get(1);

        CouponIssueValidationResult validationResult = CouponIssueValidationResult.fromCode(statusCode);
        validationResult.handleOrThrow();
        log.info("Coupon issue success: couponId={}, userId={}, count={}", couponId, userId, count);
    }

    @Transactional
    public void processCouponIssue(long couponId, long userId) {

        // 1. Save coupon issuance history
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        couponIssueRepository.save(couponIssue);

        // 2. Increment issued coupon quantity
        couponRepository.incrementIssuedQuantity(couponId);

        // TODO 3. Publish event (e.g. for analytics, notifications, etc.)
        log.info("Publish event (e.g. for analytics, notifications, etc.)");
    }
}
