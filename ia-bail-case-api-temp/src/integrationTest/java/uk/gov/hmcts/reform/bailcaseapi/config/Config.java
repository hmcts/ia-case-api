package uk.gov.hmcts.reform.bailcaseapi.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
