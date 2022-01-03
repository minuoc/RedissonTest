package com.redisson;

import org.redisson.Redisson;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class MultiLockExample {
    public static void main(String[] args) throws InterruptedException {
        RedissonClient redissonClient = Redisson.create();
        RLock lock1 = redissonClient.getLock("lock1");
        RLock lock2 = redissonClient.getLock("lock2");
        RLock lock3 = redissonClient.getLock("lock3");

        Thread t = new Thread(()-> {
            RedissonMultiLock lock = new RedissonMultiLock(lock1,lock2,lock3);
            lock.lock();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {

            }
            lock.unlock();
        });
        t.start();
        t.join(1000);

        RedissonMultiLock lock = new RedissonMultiLock(lock1,lock2,lock3);
        lock.lock();
        System.out.println(lock.getName());
        lock.unlock();
    }
}
