package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class PinInPostActivatedTest {

    private PinInPostActivated pinInPostActivated;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;


    @BeforeEach
    public void setUp() throws Exception {
        pinInPostActivated = new PinInPostActivated();
    }

    @Test
    public void appellantPinInPost_is_generated() {
        AsylumCase asylumCase = new AsylumCase();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PIP_ACTIVATION);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostActivated.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        assertEquals(1, response.getData().size());

        Optional<JourneyType> details = response.getData().read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class);
        assertEquals(JourneyType.AIP, details.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = pinInPostActivated.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && event == Event.PIP_ACTIVATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }
}
