package com.my.coupon.service;

import com.my.coupon.dto.CacheCouponDto;
import com.my.coupon.dto.CouponIssueRequestDto;
import com.my.coupon.entity.CouponIssue;
import com.my.coupon.entity.CouponType;
import com.my.coupon.exception.CouponErrorCode;
import com.my.coupon.exception.CouponException;
import com.my.coupon.exception.SystemErrorCode;
import com.my.coupon.exception.SystemException;
import com.my.coupon.infra.CouponIssueKafkaProducer;
import com.my.coupon.repository.CouponIssueRepository;
import com.my.coupon.repository.CouponRepository;
import com.my.coupon.service.cache.CouponCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisKafkaService Unit Tests")
class RedisKafkaServiceTest {

    @Mock private CouponCacheService couponCacheService;

    @Mock private RedisScript<List> couponIssueValidationScript;

    @Mock private RedisTemplate<String, String> redisTemplate;

    @Mock private CouponIssueKafkaProducer couponIssueKafkaProducer;

    @Mock private CouponIssueRepository couponIssueRepository;

    @Mock private CouponRepository couponRepository;

    @InjectMocks private RedisKafkaService redisKafkaService;

    // Test constants
    private static final long COUPON_ID = 1L;
    private static final long USER_ID = 100L;
    private static final int TOTAL_QUANTITY = 100;
    private static final CouponType COUPON_TYPE = CouponType.FIRST_COME_FIRST_SERVED;

    // Test data
    private CouponIssueRequestDto requestDto;
    private CacheCouponDto validCouponDto;

    @BeforeEach
    void setUp() {
        // Initialize test data before each test
        requestDto = new CouponIssueRequestDto(COUPON_ID, USER_ID);
        validCouponDto = createValidCouponDto();
    }

    @Test
    @DisplayName("Should issue coupon successfully when all validations pass")
    void issue_validRequest_issuesCouponSuccessfully() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(List.of(1L, 51L)); // SUCCESS status with new count

        // When
        assertThatCode(() -> redisKafkaService.issue(requestDto))
                .doesNotThrowAnyException();

        // Then
        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate).execute(eq(couponIssueValidationScript), anyList(), any(Object[].class));
        verify(couponIssueKafkaProducer).send(requestDto);
    }

    @Test
    @DisplayName("Should throw CouponException when coupon date is not available")
    void issue_dateNotAvailable_throwsCouponException() {
        // Given
        CacheCouponDto dateUnavailableCoupon = createDateUnavailableCouponDto();
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(dateUnavailableCoupon);

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(CouponException.class)
                .hasFieldOrPropertyWithValue("couponErrorCode", CouponErrorCode.INVALID_COUPON_ISSUE_DATE);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), any());
        verify(couponIssueKafkaProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should throw CouponException when coupon quantity is not available")
    void issue_quantityNotAvailable_throwsCouponException() {
        // Given
        CacheCouponDto quantityUnavailableCoupon = createQuantityUnavailableCouponDto();
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(quantityUnavailableCoupon);

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(CouponException.class)
                .hasFieldOrPropertyWithValue("couponErrorCode", CouponErrorCode.INVALID_COUPON_ISSUE_QUANTITY);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), any());
        verify(couponIssueKafkaProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should throw CouponException when user already issued coupon")
    void issue_duplicateRequest_throwsCouponException() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(List.of(-1L, 0L)); // DUPLICATE_REQUEST status

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(CouponException.class)
                .hasFieldOrPropertyWithValue("couponErrorCode", CouponErrorCode.DUPLICATED_COUPON_ISSUE);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate).execute(eq(couponIssueValidationScript), anyList(), any(Object[].class));
        verify(couponIssueKafkaProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should throw CouponException when coupon is sold out")
    void issue_soldOutCoupon_throwsCouponException() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(List.of(-2L, 100L)); // SOLD_OUT status

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(CouponException.class)
                .hasFieldOrPropertyWithValue("couponErrorCode", CouponErrorCode.INVALID_COUPON_ISSUE_QUANTITY);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate).execute(eq(couponIssueValidationScript), anyList(), any(Object[].class));
        verify(couponIssueKafkaProducer, never()).send(any());
    }



    @Test
    @DisplayName("Should throw SystemException when Redis script execution returns null")
    void issue_redisScriptReturnsNull_throwsSystemException() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(null); // Script execution failure

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue("systemErrorCode", SystemErrorCode.REDIS_SCRIPT_EXECUTION_FAILED);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate).execute(eq(couponIssueValidationScript), anyList(), any(Object[].class));
        verify(couponIssueKafkaProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should throw SystemException when Redis script execution returns empty result")
    void issue_redisScriptReturnsEmpty_throwsSystemException() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(Collections.emptyList()); // Empty result

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue("systemErrorCode", SystemErrorCode.REDIS_SCRIPT_EXECUTION_FAILED);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate).execute(eq(couponIssueValidationScript), anyList(), any(Object[].class));
        verify(couponIssueKafkaProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should propagate exception when Kafka producer fails")
    void issue_kafkaProducerFailure_propagatesException() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(List.of(1L, 51L)); // SUCCESS status

        // Kafka producer throws SystemException
        doThrow(new SystemException(SystemErrorCode.KAFKA_SEND_FAILED))
                .when(couponIssueKafkaProducer).send(requestDto);

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.issue(requestDto))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue("systemErrorCode", SystemErrorCode.KAFKA_SEND_FAILED);

        verify(couponCacheService).getCouponLocalCache(COUPON_ID);
        verify(redisTemplate).execute(eq(couponIssueValidationScript), anyList(), any(Object[].class));
        verify(couponIssueKafkaProducer).send(requestDto);
    }

    @Test
    @DisplayName("Should verify Redis script is called with correct parameters")
    void issue_validRequest_callsRedisScriptWithCorrectParameters() {
        // Given
        when(couponCacheService.getCouponLocalCache(COUPON_ID)).thenReturn(validCouponDto);
        when(redisTemplate.execute(eq(couponIssueValidationScript), anyList(), any(Object[].class)))
                .thenReturn(List.of(1L, 51L));

        // When
        redisKafkaService.issue(requestDto);

        // Then
        verify(redisTemplate).execute(
                eq(couponIssueValidationScript),
                anyList(),
                any(Object[].class)
        );
    }

    @Test
    @DisplayName("Should process coupon issue successfully when all database operations complete")
    void processCouponIssue_validRequest_processesSuccessfully() {
        // Given
        CouponIssue savedCouponIssue = CouponIssue.builder()
                .id(1L)
                .couponId(COUPON_ID)
                .userId(USER_ID)
                .build();

        when(couponIssueRepository.save(any(CouponIssue.class))).thenReturn(savedCouponIssue);

        // When
        assertThatCode(() -> redisKafkaService.processCouponIssue(COUPON_ID, USER_ID))
                .doesNotThrowAnyException();

        // Then
        verify(couponIssueRepository).save(argThat(couponIssue ->
                couponIssue.getCouponId().equals(COUPON_ID) &&
                        couponIssue.getUserId().equals(USER_ID)
        ));
        verify(couponRepository).incrementIssuedQuantity(COUPON_ID);
    }

    @Test
    @DisplayName("Should handle repository failure during coupon issue processing")
    void processCouponIssue_repositoryFailure_throwsException() {
        // Given
        when(couponIssueRepository.save(any(CouponIssue.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> redisKafkaService.processCouponIssue(COUPON_ID, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(couponIssueRepository).save(any(CouponIssue.class));
        verify(couponRepository, never()).incrementIssuedQuantity(anyLong());
    }

    // Helper methods for creating test data
    private CacheCouponDto createValidCouponDto() {
        return new CacheCouponDto(
                COUPON_ID,
                COUPON_TYPE,
                TOTAL_QUANTITY,
                true,  // Date is available
                true   // Quantity is available
        );
    }

    private CacheCouponDto createDateUnavailableCouponDto() {
        return new CacheCouponDto(
                COUPON_ID,
                COUPON_TYPE,
                TOTAL_QUANTITY,
                false, // Date is not available
                true   // Quantity is available
        );
    }

    private CacheCouponDto createQuantityUnavailableCouponDto() {
        return new CacheCouponDto(
                COUPON_ID,
                COUPON_TYPE,
                TOTAL_QUANTITY,
                true,  // Date is available
                false  // Quantity is not available
        );
    }
}