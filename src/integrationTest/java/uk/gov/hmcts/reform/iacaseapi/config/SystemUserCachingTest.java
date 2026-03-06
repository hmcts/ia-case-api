package uk.gov.hmcts.reform.iacaseapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.CacheConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@Import({ CacheConfiguration.class, IdamService.class })
@ExtendWith(SpringExtension.class)
@EnableCaching
public class SystemUserCachingTest {

    private static final String BEARER_AUTH = "Bearer ";
    private static final String TOKEN = "SOME_TOKEN";

    @Autowired
    private IdamService idamService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private RoleAssignmentService ras;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void setUp() {
        given(idamApi.token(any()))
                .willReturn(new Token(TOKEN, BEARER_AUTH));
    }

    @Test
    void givenRedisCaching_whenFindItemById_thenItemReturnedFromCache() {
        String itemCacheMiss = idamService.getServiceUserToken();
        String itemCacheHit = idamService.getServiceUserToken();

        assertThat(itemCacheMiss).isEqualTo(BEARER_AUTH + TOKEN);
        assertThat(itemCacheHit).isEqualTo(BEARER_AUTH + TOKEN);

        verify(idamApi, times(1)).token(any());
    }
}
