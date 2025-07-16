package com.my.coupon.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    RedissonClient redissonClient() {
        String address = "redis://" + host + ":" + port;
        Config cfg = new Config();
        cfg.useSingleServer()
                .setAddress(address)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(16);
        return Redisson.create(cfg);
    }


}
