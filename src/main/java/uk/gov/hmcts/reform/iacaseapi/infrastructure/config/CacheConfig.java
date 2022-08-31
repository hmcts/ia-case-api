package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(1, TimeUnit.MINUTES);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("cacheId","event");
        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }
}

