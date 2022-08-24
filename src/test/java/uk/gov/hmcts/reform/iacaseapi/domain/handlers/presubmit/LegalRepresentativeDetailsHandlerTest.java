package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepresentativeDetailsHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    private LegalRepresentativeDetailsHandler legalRepresentativeDetailsHandler;

    @BeforeEach
    public void setUp() {

        legalRepresentativeDetailsHandler =
            new LegalRepresentativeDetailsHandler(userDetails);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL" })
    void should_set_legal_representative_details_into_the_case_for_submit_appeal(Event event) {

        final String expectedLegalRepresentativeName = "John Doe";
        final String expectedLegalRepresentativeEmailAddress = "john.doe@example.com";
        final String expectedLegalRepCompany = "";
        final String expectedLegalRepName = "";

        when(userDetails.getForename()).thenReturn("John");
        when(userDetails.getSurname()).thenReturn("Doe");
        when(userDetails.getEmailAddress()).thenReturn(expectedLegalRepresentativeEmailAddress);

        when(callback.getEvent()).thenReturn(event);

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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL" })
    void should_set_legal_representative_details_into_the_case_for_pay_and_submit_appeal(Event event) {

        final String expectedLegalRepresentativeName = "John Doe";
        final String expectedLegalRepresentativeEmailAddress = "john.doe@example.com";
        final String expectedLegalRepCompany = "";
        final String expectedLegalRepName = "";

        when(userDetails.getForename()).thenReturn("John");
        when(userDetails.getSurname()).thenReturn("Doe");
        when(userDetails.getEmailAddress()).thenReturn(expectedLegalRepresentativeEmailAddress);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL" })
    void should_not_overwrite_existing_legal_representative_details_for_submit_appeal(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL" })
    void should_not_overwrite_existing_legal_representative_details_for_pay_and_submit_appeal(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> legalRepresentativeDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = legalRepresentativeDetailsHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    SUBMIT_APPEAL)
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
    void it_cannot_handle_callback_for_aip() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);

        boolean canHandle = legalRepresentativeDetailsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeDetailsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> legalRepresentativeDetailsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
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
