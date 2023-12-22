package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AutoHearingRequestServiceTest {

    @Mock
    private FeatureToggler featureToggler;

    private AutoHearingRequestService service;

    @BeforeEach
    void setup() {
        when(featureToggler.getValue("auto-hearing-request-feature", false)).thenReturn(true);

        service = new AutoHearingRequestService(featureToggler);
    }

    @Test
    void autoHearingEnabled_should_return_true() {

        assertTrue(service.autoHearingRequestEnabled());
    }

    @Test
    void autoHearingEnabled_should_return_false() {
        when(featureToggler.getValue("auto-hearing-request-feature", false)).thenReturn(false);

        assertFalse(service.autoHearingRequestEnabled());
    }
}
