package com.redisson;

import org.redisson.Redisson;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RedissonMultiLockInit {
    private final ArrayList<RLock> rLockList = new ArrayList<>();
    @Autowired
    RedissonClient redissonClient;

    public RedissonMultiLock initLock(String... locksName) {
        for (String lockName : locksName) {
            rLockList.add(redissonClient.getLock(lockName));
        }

        RLock[] rLocks = rLockList.toArray(new RLock[0]);
        return new RedissonMultiLock(rLocks);
    }

    public List<RLock> getRLocks() {
        return rLockList;
    }
}
