package com.my.coupon.infra;

import com.my.coupon.service.RedisKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueKafkaConsumer {
    private final RedisKafkaService redisKafkaService;

    @KafkaListener(topics = "coupon.issue", groupId = "coupon-consumer-group")
    public void consume(String message) {
        try {
            String[] parts = message.split(":");
            long couponId = Long.parseLong(parts[0]);
            long userId = Long.parseLong(parts[1]);

            log.info("Received Kafka coupon issue request: couponId={}, userId={}", couponId, userId);

            redisKafkaService.processCouponIssue(couponId, userId);
        } catch (Exception e) {
            log.error("Failed to process Kafka coupon issue message: message={}", message, e);
            // TODO: Add error handling logic (e.g., retry or dead-letter queue)
        }
    }

}
