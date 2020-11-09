package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativeDetailsHandlerTest {

    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    private LegalRepresentativeDetailsHandler legalRepresentativeDetailsHandler;

    @Before
    public void setUp() {

        legalRepresentativeDetailsHandler =
            new LegalRepresentativeDetailsHandler(userDetailsProvider);
    }

    @Test
    public void should_be_handled_at_earliest_point() {
        assertEquals(DispatchPriority.EARLIEST, legalRepresentativeDetailsHandler.getDispatchPriority());
    }

    @Test
    public void should_set_legal_representative_details_into_the_case_for_submit_appeal() {

        final String expectedLegalRepresentativeName = "John Doe";
        final String expectedLegalRepresentativeEmailAddress = "john.doe@example.com";
        final String expectedLegalRepCompany = "";
        final String expectedLegalRepName = "";

        when(userDetails.getForename()).thenReturn("John");
        when(userDetails.getSurname()).thenReturn("Doe");
        when(userDetails.getEmailAddress()).thenReturn(expectedLegalRepresentativeEmailAddress);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LEGAL_REPRESENTATIVE_NAME)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_NAME)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_NAME, expectedLegalRepresentativeName);
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, expectedLegalRepresentativeEmailAddress);
        verify(asylumCase, times(1)).write(LEGAL_REP_COMPANY, expectedLegalRepCompany);
        verify(asylumCase, times(1)).write(LEGAL_REP_NAME, expectedLegalRepName);
    }

    @Test
    public void should_set_legal_representative_details_into_the_case_for_pay_and_submit_appeal() {

        final String expectedLegalRepresentativeName = "John Doe";
        final String expectedLegalRepresentativeEmailAddress = "john.doe@example.com";
        final String expectedLegalRepCompany = "";
        final String expectedLegalRepName = "";

        when(userDetails.getForename()).thenReturn("John");
        when(userDetails.getSurname()).thenReturn("Doe");
        when(userDetails.getEmailAddress()).thenReturn(expectedLegalRepresentativeEmailAddress);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LEGAL_REPRESENTATIVE_NAME)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_NAME)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_NAME, expectedLegalRepresentativeName);
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, expectedLegalRepresentativeEmailAddress);
        verify(asylumCase, times(1)).write(LEGAL_REP_COMPANY, expectedLegalRepCompany);
        verify(asylumCase, times(1)).write(LEGAL_REP_NAME, expectedLegalRepName);
    }

    @Test
    public void should_not_overwrite_existing_legal_representative_details_for_submit_appeal() {

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_NAME)).thenReturn(Optional.of("existing"));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS)).thenReturn(Optional.of("existing"));
        when(asylumCase.read(LEGAL_REP_COMPANY)).thenReturn(Optional.of("existing"));
        when(asylumCase.read(LEGAL_REP_NAME)).thenReturn(Optional.of("existing"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    public void should_not_overwrite_existing_legal_representative_details_for_pay_and_submit_appeal() {

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_NAME)).thenReturn(Optional.of("existing"));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS)).thenReturn(Optional.of("existing"));
        when(asylumCase.read(LEGAL_REP_COMPANY)).thenReturn(Optional.of("existing"));
        when(asylumCase.read(LEGAL_REP_NAME)).thenReturn(Optional.of("existing"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = legalRepresentativeDetailsHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL)
                        .contains(callback.getEvent())
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
