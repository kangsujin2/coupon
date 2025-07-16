package com.my.coupon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.coupon.infra.lock.DistributeLockExecutor;
import com.my.coupon.dto.CacheCouponDto;
import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.entity.CouponIssue;
import com.my.coupon.event.model.CouponIssuedEvent;
import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.ErrorCode;
import com.my.coupon.infra.redis.CouponRedisTemplate;
import com.my.coupon.repository.CouponIssueRepository;
import com.my.coupon.repository.CouponRepository;
import com.my.coupon.service.cache.CouponCacheService;
import com.my.coupon.service.validator.CouponValidator;
import com.my.coupon.util.RedisKeyGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@Transactional
@RequiredArgsConstructor
public class RedisCouponService implements CouponService {

    private final CouponCacheService couponCacheService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponValidator couponValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouponRedisTemplate couponRedisTemplate;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void issue(CouponIssueRequestDto requestDto) {
        long couponId = requestDto.couponId();
        long userId = requestDto.userId();

        CacheCouponDto couponDto = couponCacheService.getCouponLocalCache(couponId);
        couponDto.isCouponAvailable();

        distributeLockExecutor.execute(
                "coupon:lock%s".formatted(couponId),
                3000,
                3000,
                () -> {
                    couponValidator.isCouponIssuable(couponId, userId, couponDto.totalQuantity());
                    enqueueIssueRequest(couponId, userId);
                }
        );
    }

    private void enqueueIssueRequest(long couponId, long userId) {
        CouponIssueRequestDto issueRequestDto = new CouponIssueRequestDto(couponId, userId);

        try {
            String value = objectMapper.writeValueAsString(issueRequestDto);
            couponRedisTemplate.sAdd(RedisKeyGenerator.couponIssueSetKey(couponId), String.valueOf(userId));
            couponRedisTemplate.rPush(RedisKeyGenerator.couponIssueQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST);
        }
    }

    public void processCouponIssue(long couponId, long userId) {
        saveCouponIssue(couponId, userId);
        couponRepository.incrementIssuedQuantity(couponId);
        applicationEventPublisher.publishEvent(new CouponIssuedEvent(couponId));
    }

    private CouponIssue saveCouponIssue(long couponId, long userId) {
        CouponIssue couponIssue = CouponIssue.builder().couponId(couponId).userId(userId).build();
        return couponIssueRepository.save(couponIssue);
    }
}
