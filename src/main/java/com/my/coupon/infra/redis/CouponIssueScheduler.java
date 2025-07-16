package com.my.coupon.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.service.RedisCouponService;
import com.my.coupon.util.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CouponIssueScheduler {

    private final CouponRedisTemplate couponRedisTemplate;
    private final RedisCouponService redisCouponService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 1000)
    public void consumeCouponIssueQueue() throws JsonProcessingException {
        while (hasPendingIssueRequests()) {
            CouponIssueRequestDto dto = peekCouponIssueRequest();
            redisCouponService.processCouponIssue(dto.couponId(), dto.userId());
            removeProcessedRequest();
        }
    }

    private boolean hasPendingIssueRequests() {
        return couponRedisTemplate.lSize(RedisKeyGenerator.couponIssueQueueKey()) > 0;
    }

    private CouponIssueRequestDto peekCouponIssueRequest() throws JsonProcessingException {
        return objectMapper.readValue(couponRedisTemplate.lIndex(RedisKeyGenerator.couponIssueQueueKey(), 0L), CouponIssueRequestDto.class);
    }

    private void removeProcessedRequest() {
        couponRedisTemplate.lPop(RedisKeyGenerator.couponIssueQueueKey());
    }




}
