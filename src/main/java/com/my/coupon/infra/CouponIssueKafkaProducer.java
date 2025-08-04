package com.my.coupon.infra;

import com.my.coupon.dto.CouponIssueRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "coupon.issue";

    public void send(CouponIssueRequestDto dto) {
        try {
            String message = dto.couponId() + ":" + dto.userId();
            kafkaTemplate.send(TOPIC, message);
            log.info("Coupon issue message sent → topic={}, message={}", TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
            // TODO: Consider implementing retry logic or a dead-letter queue
        }
    }

}
