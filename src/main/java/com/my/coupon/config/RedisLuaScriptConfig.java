package com.my.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RedisLuaScriptConfig {
    
    @Bean
    public RedisScript<List> couponIssueValidationScript() {
        String script = """
        
        -- KEYS: [count_key, set_key]
        -- ARGV: [user_id, max_count]
        
        local count_key = KEYS[1]
        local set_key = KEYS[2]
        local user_id = ARGV[1]
        local max_count = tonumber(ARGV[2])
        
        -- 1. Check for duplicate request
        local already_issued = redis.call('SISMEMBER', set_key, user_id)
        if already_issued == 1 then
            return {-1, 0}
        end
        
        -- 2. Get current issued count
        local current_count = redis.call('GET', count_key) or 0
        current_count = tonumber(current_count)
        
        -- 3. Check if quota exceeded
        if current_count >= max_count then
            return {-2, current_count}
        end
        
        -- 4. Atomically reserve coupon for user
        local new_count = redis.call('INCR', count_key)
        redis.call('SADD', set_key, user_id)
        redis.call('EXPIRE', count_key, 3600)
        redis.call('EXPIRE', set_key, 3600)
        
        return {1, new_count}
        """;

        return RedisScript.of(script, List.class);
    }

    @Bean
    public RedisScript<List> rollbackScript() {
        String script = """
            -- KEYS: [count_key, set_key]
            -- ARGV: [user_id]
            
            local count_key = KEYS[1]
            local set_key = KEYS[2]
            local user_id = ARGV[1]
            
            -- Check if user actually exists in the set
            local user_exists = redis.call('SISMEMBER', set_key, user_id)
            if user_exists == 1 then
                -- Remove user from set
                redis.call('SREM', set_key, user_id)
                -- Decrement count
                local new_count = redis.call('DECR', count_key)
                return {1, new_count}
            else
                return {0, 0}
            end
            """;

        return RedisScript.of(script, List.class);
    }
}
