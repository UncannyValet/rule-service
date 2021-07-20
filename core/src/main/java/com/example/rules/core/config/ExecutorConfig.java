package com.example.rules.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

    @Value("${executor.arbiter.size.core:4}")
    private int arbiterPoolSize;

    @Value("${executor.investigator.size.core:10}")
    private int investigatorPoolSize;

    @Bean("arbiterPool")
    public AsyncTaskExecutor arbiterExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(arbiterPoolSize);
        executor.setMaxPoolSize(arbiterPoolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Arbiter");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    @Bean("investigatorPool")
    public AsyncTaskExecutor investigatorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(investigatorPoolSize);
        executor.setMaxPoolSize(investigatorPoolSize);
        executor.setQueueCapacity(arbiterPoolSize * 10);
        executor.setThreadNamePrefix("Investigator");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }
}
