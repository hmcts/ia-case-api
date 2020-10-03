package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

public class IACaseApiCacheResolver implements CacheResolver {

    private final CacheManager simpleCacheManager;

    public IACaseApiCacheResolver(CacheManager simpleCacheManager) {
        this.simpleCacheManager = simpleCacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();
        if ("getUserDetails".equals(context.getMethod().getName())) {
            caches.add(simpleCacheManager.getCache("IdamUserDetails"));
        }
        return caches;
    }
}
