package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AutoRequestHearingServiceTest {

    @Mock
    private IaHearingsApiService iaHearingsApiService;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AutoRequestHearingService autoRequestHearingService;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        autoRequestHearingService = new AutoRequestHearingService(iaHearingsApiService, locationBasedFeatureToggler);
    }

    @ParameterizedTest
    @CsvSource({"YES, YES, true, true",
        "NO, YES, true, false",
        "YES, NO, true, false",
        "YES, YES, false, false"})
    void shouldAutoRequestHearing_should_work_as_expected(
        YesOrNo integrated, YesOrNo autoRequestEnabled, boolean canAutoRequest, boolean expected) {

        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(integrated));
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(autoRequestEnabled);

        assertEquals(expected, autoRequestHearingService.shouldAutoRequestHearing(asylumCase, canAutoRequest));
    }

    @Test
    void autoCreateHearing_should_successfully_call_hearings_service() {
        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(asylumCase);

        autoRequestHearingService.autoCreateHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CREATE_HEARING_REQUIRED, NO);
        verify(iaHearingsApiService, times(1)).aboutToSubmit(callback);
    }

    @Test
    void autoCreateHearing_should_set_request_status_when_call_fails() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);

        doThrow(AsylumCaseServiceResponseException.class).when(iaHearingsApiService).aboutToSubmit(callback);

        autoRequestHearingService.autoCreateHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CREATE_HEARING_REQUIRED, YES);
    }

    @Test
    void autoUpdateHearing_should_set_request_status_when_call_fails() {
        when(callback.getEvent()).thenReturn(Event.RECORD_ADJOURNMENT_DETAILS);

        doThrow(AsylumCaseServiceResponseException.class).when(iaHearingsApiService).aboutToSubmit(callback);

        autoRequestHearingService.autoUpdateHearing(callback);

        verify(asylumCase, times(1)).write(UPDATE_HMC_REQUEST_SUCCESS, NO);
    }

    @Test
    void autoCancelHearing_should_set_request_status_when_call_fails() {
        when(callback.getEvent()).thenReturn(Event.RECORD_ADJOURNMENT_DETAILS);

        doThrow(AsylumCaseServiceResponseException.class).when(iaHearingsApiService).aboutToSubmit(callback);

        autoRequestHearingService.autoCancelHearing(callback);

        verify(asylumCase, times(1)).write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
    }

    @Test
    void should_return_correct_confirmation_header_and_body_when_request_is_successful() {
        String body = "#### What happens next\n\n"
                      + "The hearing request has been created and is visible on the [Hearings tab]"
                      + "(/cases/case-details/1/hearings)";
        String header = "# Hearing listed";
        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(NO));

        PostSubmitCallbackResponse response = autoRequestHearingService
            .buildAutoHearingRequestConfirmation(asylumCase, header, 1L);

        assertEquals(response.getConfirmationHeader().orElse(null), header);
        assertEquals(response.getConfirmationBody().orElse(null), body);
    }

    @Test
    void should_return_correct_confirmation_header_and_body_when_request_is_unsuccessful() {
        String body = "![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                      + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                      + "\n\n"
                      + "#### What happens next\n\n"
                      + "The hearing could not be auto-requested. Please manually request the "
                      + "hearing via the [Hearings tab](/cases/case-details/1/hearings)";
        String header = "";
        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YES));

        PostSubmitCallbackResponse response = autoRequestHearingService
            .buildAutoHearingRequestConfirmation(asylumCase, header, 1L);

        assertEquals(header, response.getConfirmationHeader().orElse(null));
        assertEquals(body, response.getConfirmationBody().orElse(null));
    }

}
