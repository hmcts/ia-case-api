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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DeletionDateProviderTest {
    private DeletionDateProvider deletionDateProvider;

    @Mock
    DateProvider dateProvider;
    @Mock
    LocalDateTime localTime;
    @Mock
    LocalDateTime deletionTime;

    @BeforeEach
    void setUp() {
        deletionDateProvider = new DeletionDateProvider(dateProvider, 365);
    }

    @Test
    void should_return_deletion_date() {
        // given
        when(dateProvider.nowWithTime()).thenReturn(localTime);
        when(localTime.plusDays(365)).thenReturn(deletionTime);

        // when
        LocalDateTime res = deletionDateProvider.getDeletionTime();

        // then
        assertEquals(deletionTime, res);
        verify(dateProvider).nowWithTime();
        verify(localTime).plusDays(365);
    }
}