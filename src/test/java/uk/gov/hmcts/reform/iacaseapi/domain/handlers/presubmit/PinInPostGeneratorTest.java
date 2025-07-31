package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class PinInPostGeneratorTest {

    private static final long ACCESS_CODE_EXPIRY_DAYS = 90;

    private PinInPostGenerator pinInPostGenerator;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;


    @BeforeEach
    public void setUp() throws Exception {
        pinInPostGenerator = new PinInPostGenerator(ACCESS_CODE_EXPIRY_DAYS);
    }

    @Test
    public void appellantPinInPost_is_generated() {
        AsylumCase asylumCase = new AsylumCase();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REMOVE_LEGAL_REPRESENTATIVE);

        PreSubmitCallbackResponse<AsylumCase> response = pinInPostGenerator.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        assertEquals(1, response.getData().size());

        Optional<PinInPostDetails> details = response.getData().read(AsylumCaseFieldDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class);

        assertTrue(details.isPresent());
        assertNotNull(details.get().getAccessCode());
        assertNotNull(details.get().getExpiryDate());
        assertNotNull(details.get().getPinUsed());
        assertEquals(LocalDate.now().plusDays(ACCESS_CODE_EXPIRY_DAYS).toString(), details.get().getExpiryDate());
        assertEquals(YesOrNo.NO, details.get().getPinUsed());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = pinInPostGenerator.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (event == Event.REMOVE_REPRESENTATION
                            || event == Event.REMOVE_LEGAL_REPRESENTATIVE
                            || event == Event.GENERATE_PIN_IN_POST)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

}
