package com.my.coupon.infra;

import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.exception.SystemErrorCode;
import com.my.coupon.exception.SystemException;
import com.my.coupon.util.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> rollbackScript;
    private static final String TOPIC = "coupon.issue";

    public void send(CouponIssueRequestDto dto) {
        try {
            String message = dto.couponId() + ":" + dto.userId();
            kafkaTemplate.send(TOPIC, message);
            log.info("Coupon issue message sent → topic={}, message={}", TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
            rollbackRedisState(dto.couponId(), dto.userId());
            throw new SystemException(SystemErrorCode.KAFKA_SEND_FAILED);
        }
    }

    private void rollbackRedisState(long couponId, long userId) {
        try {
            String countKey = RedisKeyGenerator.couponIssueCountKey(couponId);
            String setKey = RedisKeyGenerator.couponIssueSetKey(couponId);

            redisTemplate.execute(rollbackScript,
                    List.of(countKey, setKey),
                    List.of(String.valueOf(userId)));

            log.info("Redis state rolled back successfully: couponId={}, userId={}",
                    couponId, userId);

        } catch (Exception e) {
            log.error("Critical: Failed to rollback Redis state", e);
            throw new SystemException(SystemErrorCode.REDIS_ROLLBACK_FAILED);
            // TODO: Send alert notification (Slack, email, etc.)
        }
    }

}
