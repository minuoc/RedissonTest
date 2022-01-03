package com.redisson;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/lock")
public class LockController {
    @Autowired
    RedissonMultiLockInit redissonMultiLockInit;

    @Autowired
    PlatformTransactionManager transactionManager;

    @GetMapping("/get/{waitTime}/{leaseTime}")
    @ResponseBody
    public String getLock(@PathVariable long waitTime, @PathVariable long leaseTime) throws InterruptedException {
        String[] strings = {"test1", "test2", "test3"};
        RedissonMultiLock lock = redissonMultiLockInit.initLock(strings);
        //手动开启事务管理，@Transitional无法控制redis的分布式锁
        //创建事务定义对象
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //设置是否只读，false支持事务
        def.setReadOnly(false);
        //设置事务隔离级别，可以重复读mysql默认级别
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        //设置事务传播行为
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        //配置事务管理器
        TransactionStatus status = transactionManager.getTransaction(def);
        if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
            System.out.println(Thread.currentThread().getName() + " waiting time is " + waitTime + "s " +
                    "leaseTime is " + leaseTime + "s " +
                    "execute time is " + (leaseTime + 10) + " s");
            try {

                //模拟执行超时释放锁
                Thread.sleep((leaseTime + 10) * 1000);
                List<RLock> rLocks = redissonMultiLockInit.getRLocks();
                //判断是否仍然持有所有锁，防止锁过期
                if (rLocks.stream().allMatch(RLock::isLocked)) {
                        //提交业务
                        transactionManager.commit(status);
                    //提交业务后再释放分布式锁
                    lock.unlock();
                    return "unlock success,transition success";
                } else {
                    //回滚业务
                    transactionManager.rollback(status);
                    return "lock is expired,transition fail";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "transition error";
            }
        } else {
            return Thread.currentThread().getName() + " can't get the lock,because the waiting time isn't enough. Waiting time is " + waitTime + "s, " +
                    "leaseTime is " + leaseTime + "s ";
        }
    }
}
