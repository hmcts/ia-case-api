package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Test;

public class CachingAppealReferenceNumberGeneratorTest {

    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator = mock(AppealReferenceNumberGenerator.class);

    @Test
    public void returns_cached_item_on_cache_hit() {
        CachingAppealReferenceNumberGenerator underTest =
                new CachingAppealReferenceNumberGenerator(
                        60,
                        appealReferenceNumberGenerator);

        when(appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(same("some-type")))
                .thenReturn(Optional.of("some-ref"));

        Optional<String> maybeAppealReferenceNumber1 = underTest.getNextAppealReferenceNumberFor(
                1,
                "some-type");

        verify(appealReferenceNumberGenerator, times(1))
                .getNextAppealReferenceNumberFor(same("some-type"));

        Optional<String> maybeAppealReferenceNumber2 = underTest.getNextAppealReferenceNumberFor(
                1,
                "some-type");

        verifyNoMoreInteractions(appealReferenceNumberGenerator);

        assertThat(maybeAppealReferenceNumber1.get()).isEqualTo("some-ref");
        assertThat(maybeAppealReferenceNumber2.get()).isEqualTo("some-ref");
    }

    @Test
    public void delegates_on_cache_miss() {

        CachingAppealReferenceNumberGenerator underTest =
                new CachingAppealReferenceNumberGenerator(
                        60,
                        appealReferenceNumberGenerator);

        when(appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(same("some-type")))
                .thenReturn(Optional.of("some-ref"));

        Optional<String> maybeAppealReferenceNumber1 = underTest.getNextAppealReferenceNumberFor(
                1,
                "some-type");

        Optional<String> maybeAppealReferenceNumber2 = underTest.getNextAppealReferenceNumberFor(
                2,
                "some-type");

        verify(appealReferenceNumberGenerator, times(2))
                .getNextAppealReferenceNumberFor(same("some-type"));

        assertThat(maybeAppealReferenceNumber1.get()).isEqualTo("some-ref");
        assertThat(maybeAppealReferenceNumber2.get()).isEqualTo("some-ref");
    }

    @Test
    public void expired_items_dont_get_hit() {
        CachingAppealReferenceNumberGenerator underTest =
                new CachingAppealReferenceNumberGenerator(
                        1,
                        appealReferenceNumberGenerator);

        when(appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(same("some-type")))
                .thenReturn(Optional.of("some-ref"));

        Optional<String> maybeAppealReferenceNumber1 = underTest.getNextAppealReferenceNumberFor(
                1,
                "some-type");

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue("We expect this to timeout", true);
        }

        Optional<String> maybeAppealReferenceNumber2 = underTest.getNextAppealReferenceNumberFor(
                1,
                "some-type");

        verify(appealReferenceNumberGenerator, times(2))
                .getNextAppealReferenceNumberFor(same("some-type"));

        assertThat(maybeAppealReferenceNumber1.get()).isEqualTo("some-ref");
        assertThat(maybeAppealReferenceNumber2.get()).isEqualTo("some-ref");
    }
}
