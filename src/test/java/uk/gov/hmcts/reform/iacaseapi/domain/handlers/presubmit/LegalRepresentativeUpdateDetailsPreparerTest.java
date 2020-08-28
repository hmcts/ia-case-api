package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepresentativeUpdateDetailsPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    LegalRepresentativeUpdateDetailsPreparer legalRepresentativeUpdateDetailsPreparer;

    final String legalRepCompany = "Amazing Law Firm";
    final String legalRepName = "John Doe";
    final String legalRepEmailAddress = "john.doe@example.com";
    final String legalRepReferenceNumber = "ABC-123";

    @BeforeEach
    void setUp() {

        legalRepresentativeUpdateDetailsPreparer = new LegalRepresentativeUpdateDetailsPreparer();
    }

    @Test
    void prepare_fields_test() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of(legalRepCompany));
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeUpdateDetailsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(LEGAL_REP_COMPANY, String.class);
        verify(asylumCase).read(LEGAL_REP_NAME, String.class);
        verify(asylumCase).read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class);
        verify(asylumCase).read(LEGAL_REP_REFERENCE_NUMBER, String.class);

        verify(asylumCase, times(1)).write(eq(UPDATE_LEGAL_REP_COMPANY), eq(legalRepCompany));
        verify(asylumCase, times(1)).write(eq(UPDATE_LEGAL_REP_NAME), eq(legalRepName));
        verify(asylumCase, times(1)).write(eq(UPDATE_LEGAL_REP_EMAIL_ADDRESS), eq(legalRepEmailAddress));
        verify(asylumCase, times(1)).write(eq(UPDATE_LEGAL_REP_REFERENCE_NUMBER), eq(legalRepReferenceNumber));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = legalRepresentativeUpdateDetailsPreparer.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}