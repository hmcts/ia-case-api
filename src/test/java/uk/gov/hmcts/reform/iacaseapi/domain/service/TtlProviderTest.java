package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TtlProviderTest {
    private TtlProvider ttlProvider;

    @Mock
    DateProvider dateProvider;

    @BeforeEach
    void setUp() {
        ttlProvider = new TtlProvider(dateProvider);
    }

    @Test
    void should_return_ttl() {
        // given
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        // when
        String ttl = ttlProvider.getTtl();

        // then
        assertNotNull(ttl);
        verify(dateProvider).nowWithTime();
    }
}