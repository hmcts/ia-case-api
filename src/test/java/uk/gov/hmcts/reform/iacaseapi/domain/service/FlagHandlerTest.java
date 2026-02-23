package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.DETAINED_INDIVIDUAL;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;

@ExtendWith(MockitoExtension.class)
class FlagHandlerTest {

    @Mock
    private AsylumCase asylumCase;
    
    @Mock
    private DateProvider dateProvider;

    @Test
    void shouldActivateFlagWhenNoExistingFlag() {
        // Given
        String timeNow = "2023-01-01T10:00:00";
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.parse(timeNow));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)).thenReturn(Optional.empty());
        
        // When
        boolean result = FlagHandler.activateFlag(asylumCase, APPELLANT_LEVEL_FLAGS, DETAINED_INDIVIDUAL, dateProvider);
        
        // Then
        assertTrue(result);
        verify(asylumCase).write(eq(APPELLANT_LEVEL_FLAGS), any(StrategicCaseFlag.class));
    }
    
    @Test
    void shouldHandleMultipleFlagActivations() {
        // Given
        String timeNow = "2023-01-01T10:00:00";
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.parse(timeNow));
        
        // First call returns empty, simulating no existing flag
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.empty());
        
        // When
        FlagHandler.activateFlag(asylumCase, APPELLANT_LEVEL_FLAGS, DETAINED_INDIVIDUAL, dateProvider);
        
        // Then
        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any(StrategicCaseFlag.class));
    }
} 