package com.example.rules.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
//@EnableJpaRepositories("com.example.rules.core.repository")
//@EnableTransactionManagement
public class ExecutorConfig {

    @Bean("arbiterPool")
    public AsyncTaskExecutor arbiterExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Arbiter");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    @Bean("investigatorPool")
    public AsyncTaskExecutor investigatorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Investigator");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    @Bean("kieUpdateScheduler")
    public TaskScheduler updateScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("Kie Update");
        return scheduler;
    }
}
