package com.my.coupon.infra.lock;

public interface DistributeLockExecutor {
    /**
     * @param lockKey
     * @param waitTimeMillis
     * @param leaseTimeMillis
     * @param action
     */
    void execute(String lockKey,
                 long waitTimeMillis,
                 long leaseTimeMillis,
                 Runnable action);
}
