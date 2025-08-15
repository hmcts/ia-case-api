package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CustodialSentenceDate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MarkAppealAsDetainedHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustodialSentenceDate custodialSentenceDate;
    @Mock
    private PrisonNomsNumber prisonNomsNumber;

    private MarkAppealAsDetainedHandler markAppealAsDetainedHandler;

    @BeforeEach
    public void setup() {

        markAppealAsDetainedHandler = new MarkAppealAsDetainedHandler();
    }

    @Test
    void should_mark_appeal_as_detained_and_update_case_data() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_DETAINED);

        when(asylumCase.read(PRISON_NOMS_AO, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        when(asylumCase.read(DATE_CUSTODIAL_SENTENCE_AO, CustodialSentenceDate.class))
            .thenReturn(Optional.of(custodialSentenceDate));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsDetainedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(PRISON_NOMS, prisonNomsNumber);
        verify(asylumCase).write(DATE_CUSTODIAL_SENTENCE, custodialSentenceDate);
        verify(asylumCase).write(APPELLANT_IN_DETENTION, YES);
        verify(asylumCase).write(IS_ADMIN, YES);
        verify(asylumCase).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase).clear(APPELLANT_ADDRESS);
        verify(asylumCase).clear(CONTACT_PREFERENCE);
        verify(asylumCase).clear(EMAIL);
        verify(asylumCase).clear(MOBILE_NUMBER);
        verify(asylumCase).clear(JOURNEY_TYPE);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = markAppealAsDetainedHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAppealAsDetainedHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> markAppealAsDetainedHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsDetainedHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsDetainedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_clear_appellant_address_when_detention_facility_is_other() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_DETAINED);

        when(asylumCase.read(PRISON_NOMS_AO, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        when(asylumCase.read(DATE_CUSTODIAL_SENTENCE_AO, CustodialSentenceDate.class))
            .thenReturn(Optional.of(custodialSentenceDate));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsDetainedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(PRISON_NOMS, prisonNomsNumber);
        verify(asylumCase).write(DATE_CUSTODIAL_SENTENCE, custodialSentenceDate);
        verify(asylumCase).write(APPELLANT_IN_DETENTION, YES);
        verify(asylumCase).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, never()).clear(APPELLANT_ADDRESS);
        verify(asylumCase).clear(CONTACT_PREFERENCE);
        verify(asylumCase).clear(EMAIL);
        verify(asylumCase).clear(MOBILE_NUMBER);
    }

    @Test
    void should_throw_exception_when_detention_facility_is_missing() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_AS_DETAINED);

        when(asylumCase.read(PRISON_NOMS_AO, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        when(asylumCase.read(DATE_CUSTODIAL_SENTENCE_AO, CustodialSentenceDate.class))
            .thenReturn(Optional.of(custodialSentenceDate));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markAppealAsDetainedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("detentionFacility missing on when marking as detained")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}
