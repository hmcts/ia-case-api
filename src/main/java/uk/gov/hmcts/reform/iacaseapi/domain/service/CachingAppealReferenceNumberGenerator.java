package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CachingAppealReferenceNumberGenerator {

    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;
    private final Cache<Long, String>  appealReferenceNumberCache;

    public CachingAppealReferenceNumberGenerator(
            @Value("${cache.appealReferenceNumbers.expirationTimeInSeconds}") int expirationTimeInSeconds,
            AppealReferenceNumberGenerator appealReferenceNumberGenerator)
    {
        this.appealReferenceNumberGenerator = appealReferenceNumberGenerator;

        appealReferenceNumberCache = Caffeine.newBuilder()
            .expireAfterWrite(expirationTimeInSeconds, SECONDS)
            .build();
    }

    public synchronized Optional<String> getNextAppealReferenceNumberFor(long caseId, String appealType)
    {
        String maybeCachedAppealReferenceNumber =
                appealReferenceNumberCache.getIfPresent(caseId);

        if ( maybeCachedAppealReferenceNumber != null ) {
            return Optional.of(maybeCachedAppealReferenceNumber);
        }

        Optional<String> maybeAppealReferenceNumber =
                appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(appealType);

        maybeAppealReferenceNumber.ifPresent(putInCacheFor(caseId));

        return maybeAppealReferenceNumber;
    }

    private Consumer<String> putInCacheFor(long caseId)
    {
        return newAppealReferenceNumber -> appealReferenceNumberCache.put(
                caseId,
                newAppealReferenceNumber);
    }
}
