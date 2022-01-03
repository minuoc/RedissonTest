package com.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedissonHttpSessionConfig {
    @Bean(destroyMethod="shutdown")
    public RedissonClient getRedissonClient() throws IOException {
       return Redisson.create();
    }

}
