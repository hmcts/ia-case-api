package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TtlProviderTest {
    private TtlProvider ttlProvider;

    @Mock
    DateProvider dateProvider;
    @Mock
    LocalDate localDate;
    @Mock
    LocalDate deletionDate;

    @BeforeEach
    void setUp() {
        ttlProvider = new TtlProvider(dateProvider, 365);
    }

    @Test
    void should_return_deletion_date() {
        // given
        when(dateProvider.now()).thenReturn(localDate);
        when(localDate.plusDays(365)).thenReturn(deletionDate);

        // when
        LocalDate res = ttlProvider.getTtl();

        // then
        assertEquals(deletionDate, res);
        verify(dateProvider).now();
        verify(localDate).plusDays(365);
    }
}