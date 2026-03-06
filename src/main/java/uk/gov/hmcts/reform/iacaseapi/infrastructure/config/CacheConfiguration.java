package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> cacheManager.setAllowNullValues(false);
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        try {
            redisConnectionFactory.getConnection().ping();
            log.info("Redis connection successful - using Redis for systemTokenCache");

            RedisCacheConfiguration tokenCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(3300))  // 55mins (token might expire before cache)
                    .disableCachingNullValues()
                    .serializeKeysWith(
                            RedisSerializationContext.SerializationPair
                                    .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair
                                    .fromSerializer(new GenericJackson2JsonRedisSerializer()));

            // only systemTokenCache goes to Redis, rest stay as Caffeine
            return RedisCacheManager.builder(redisConnectionFactory)
                    .withCacheConfiguration("systemUserTokenCache", tokenCacheConfig)
                    .build();

        } catch (Exception e) {
            log.warn("Redis unavailable - falling back to Caffeine for all caches: {}", e.getMessage());
            return caffeineCacheManager();
        }
    }

    // need this for test and fallback if redis is down
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "systemUserTokenCache", "homeOfficeReferenceDataCache", "userInfoCache"
        );
        cacheManager.setAllowNullValues(false);
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(3300, TimeUnit.SECONDS));

        log.info("Caffeine connection");
        return cacheManager;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.url}") String redisUrl,
            @Value("${spring.data.redis.secret}") String accessKey) {

        log.info("redis url: " + redisUrl);

        if (redisUrl == null || redisUrl.isBlank()) {
            log.warn("No Redis URL configured - falling back to Caffeine");
            // return a dummy factory - cacheManager will catch the ping failure and fall back
            return new LettuceConnectionFactory();
        }

        try {
            RedisURI redisURI = RedisURI.create(redisUrl);
            redisURI.setTimeout(Duration.ofSeconds(10)); // 64seconds is default, so fail quicker

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisURI.getHost());
            config.setPort(redisURI.getPort());

            if (accessKey != null && !accessKey.isBlank()) {
                config.setPassword(RedisPassword.of(accessKey));
                log.info("adding password to redis");
            }
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .useSsl()
                    .and()
                    .commandTimeout(Duration.ofSeconds(10))
                    .build();

            log.info("Successful Redis connection. Redis connection factory created for {}:{}",
                    redisURI.getHost(), redisURI.getPort());
            return new LettuceConnectionFactory(config, clientConfig);
        } catch (Exception e) {
            log.error("Failed to create Redis connection factory: {}", e.getMessage());
            throw e;
        }
    }

}