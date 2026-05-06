package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AesEncryptingRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    @Value("${spring.data.redis.encryption.key}") // Base64-encoded 32-byte key
    private String redisEncryptionKey;

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        try {
            redisConnectionFactory.getConnection().ping();

            // Idam user info config
            AesEncryptingRedisSerializer<UserInfo> userInfoSerializer =
                new AesEncryptingRedisSerializer<>(
                    new Jackson2JsonRedisSerializer<>(UserInfo.class),
                    redisEncryptionKey
                );

            RedisCacheConfiguration userInfoCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(1800))
                .disableCachingNullValues()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(userInfoSerializer));

            // system user token config
            AesEncryptingRedisSerializer<String> tokenSerializer =
                new AesEncryptingRedisSerializer<>(
                    new Jackson2JsonRedisSerializer<>(String.class),
                    redisEncryptionKey
                );

            RedisCacheConfiguration tokenCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(1800))  // 30mins (token might expire before cache)
                .disableCachingNullValues()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(tokenSerializer));

            return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(tokenCacheConfig)
                .withCacheConfiguration("systemUserTokenCache", tokenCacheConfig)
                .withCacheConfiguration("userInfoCache", userInfoCacheConfig)
                // caches for functional tests
                .withCacheConfiguration("legalRepATokenBailCache", tokenCacheConfig)
                .withCacheConfiguration("adminOfficerTokenBailCache", tokenCacheConfig)
                .withCacheConfiguration("homeOfficeBailTokenBailCache", tokenCacheConfig)
                .withCacheConfiguration("homeOfficeGenericTokenBailCache", tokenCacheConfig)
                .withCacheConfiguration("legalRepShareCaseATokenBailCache", tokenCacheConfig)
                .withCacheConfiguration("legalRepOrgSuccessTokenBailCache", tokenCacheConfig)
                .withCacheConfiguration("judgeTokenBailCache", tokenCacheConfig)
                .build();

        } catch (Exception e) {
            // if redis is down, dont cache make idam calls, until pod restarts
            log.warn("Redis unavailable: {}", e.getMessage());
            return new NoOpCacheManager();
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
        @Value("${spring.data.redis.url}") String redisUrl,
        @Value("${spring.data.redis.secret}") String accessKey) {

        if (redisUrl == null || redisUrl.isBlank()) {
            log.warn("No Redis URL configured.");
            // return a dummy factory - cacheManager will catch the ping failure and fall back
            return new LettuceConnectionFactory();
        }

        try {
            RedisURI redisUri = RedisURI.create(redisUrl);

            boolean useSsl = redisUrl.contains("tls=true") || redisUrl.startsWith("rediss://");

            // checked azure portal,
            if (useSsl) {
                redisUri.setSsl(true);
                redisUri.setVerifyPeer(false); // for Azure (self signed certs)
            }

            redisUri.setTimeout(Duration.ofSeconds(10)); // 64seconds is default, so fail quicker

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisUri.getHost());
            config.setPort(redisUri.getPort());
            if (accessKey != null && !accessKey.isBlank()) {
                config.setPassword(RedisPassword.of(accessKey));
            }

            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .useSsl()
                .disablePeerVerification()
                .build();

            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                config,
                clientConfig
            );

            return factory;
        } catch (Exception e) {
            log.error("Failed to create Redis connection factory: {}", e.getMessage());
            throw e;
        }
    }

}
