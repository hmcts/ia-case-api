package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Import({CacheConfiguration.class})
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@EnableCaching
class CacheConfigurationTest {

    private CacheConfiguration cacheConfiguration;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    private static final String REDIS_URL_WITH_TLS = "redis://SOME_KEY@hostname.redis.cache.windows.net:6380?tls=true";
    private static final String REDIS_URL_SSL = "rediss://SOME_KEY@hostname.redis.cache.windows.net:6380";
    private static final String ACCESS_KEY = "some-access-key";
    private static final String TEST_ENCRYPTION_KEY = Base64.getEncoder().encodeToString(new byte[32]);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.url", () -> "redis://localhost:6379");
        registry.add("spring.data.redis.encryption.key", () -> TEST_ENCRYPTION_KEY);
    }

    @BeforeEach
    void setUp() {
        cacheConfiguration = new CacheConfiguration();
        ReflectionTestUtils.setField(cacheConfiguration, "redisEncryptionKey", TEST_ENCRYPTION_KEY);
    }

    @Test
    void cacheManager_shouldReturnRedisCacheManager_whenRedisAvailable() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        CacheManager result = cacheConfiguration.cacheManager(redisConnectionFactory);

        assertThat(result).isInstanceOf(RedisCacheManager.class);
        verify(redisConnectionFactory).getConnection();
    }

    @Test
    void cacheManager_shouldReturnNoOpCacheManager_whenRedisUnavailable() {
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis unavailable"));

        CacheManager result = cacheConfiguration.cacheManager(redisConnectionFactory);

        assertThat(result).isInstanceOf(NoOpCacheManager.class);
    }

    @Test
    void cacheManager_redisCacheManager_shouldContainAllCacheNames() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        CacheManager result = cacheConfiguration.cacheManager(redisConnectionFactory);
        result.getCache("systemUserTokenCache");
        result.getCache("userInfoCache");

        assertThat(result.getCacheNames())
            .contains("systemUserTokenCache", "userInfoCache");
    }

    @Test
    void cacheManager_shouldReturnNoOpCacheManager_whenPingThrowsException() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenThrow(new RuntimeException("Ping failed"));

        CacheManager result = cacheConfiguration.cacheManager(redisConnectionFactory);

        assertThat(result).isInstanceOf(NoOpCacheManager.class);
    }

    @Test
    void redisConnectionFactory_shouldReturnDefaultLettuceFactory_whenUrlIsBlank() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory("", ACCESS_KEY);

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
    }

    @Test
    void redisConnectionFactory_shouldReturnDefaultLettuceFactory_whenUrlIsNull() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory(null, ACCESS_KEY);

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
    }

    @Test
    void redisConnectionFactory_shouldCreateFactory_withTlsParameter() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory(
            REDIS_URL_WITH_TLS, ACCESS_KEY
        );

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
        LettuceConnectionFactory factory = (LettuceConnectionFactory) result;
        assertThat(factory.isUseSsl()).isTrue();
    }

    @Test
    void redisConnectionFactory_shouldCreateFactory_withRedissScheme() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory(
            REDIS_URL_SSL, ACCESS_KEY
        );

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
        LettuceConnectionFactory factory = (LettuceConnectionFactory) result;
        assertThat(factory.isUseSsl()).isTrue();
    }

    @Test
    void redisConnectionFactory_shouldCreateFactory_withAccessKey() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory(
            REDIS_URL_WITH_TLS, ACCESS_KEY
        );

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
        LettuceConnectionFactory factory = (LettuceConnectionFactory) result;
        // password is set - verify factory was created with standalone config
        assertThat(factory.getHostName()).isEqualTo("hostname.redis.cache.windows.net");
        assertThat(factory.getPort()).isEqualTo(6380);
    }

    @Test
    void redisConnectionFactory_shouldCreateFactory_withoutAccessKey() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory(
            REDIS_URL_WITH_TLS, ""
        );

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
    }

    @Test
    void redisConnectionFactory_shouldCreateFactory_withNullAccessKey() {
        RedisConnectionFactory result = cacheConfiguration.redisConnectionFactory(
            REDIS_URL_WITH_TLS, null
        );

        assertThat(result).isInstanceOf(LettuceConnectionFactory.class);
    }
}
