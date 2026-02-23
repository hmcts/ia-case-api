package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorServiceConfiguration {

    @Bean("fixedThreadPool")
    public ExecutorService fixedThreadPool(
        @Value("${executorService.threadPoolSize}") int threadPoolSize
    ) {
        return Executors.newFixedThreadPool(threadPoolSize);
    }
}
