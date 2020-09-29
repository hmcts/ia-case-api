package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HOME_OFFICE_INTEGRATION_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.ApplicationStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HomeOfficeCaseValidateHandlerTest {

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeCaseStatus homeOfficeCaseStatus;
    @Mock
    private ApplicationStatus applicationStatus;
    private boolean isHomeOfficeIntegrationEnabled = true;

    private HomeOfficeCaseValidateHandler homeOfficeCaseValidateHandler;

    @Before
    public void setUp() {

        homeOfficeCaseValidateHandler =
            new HomeOfficeCaseValidateHandler(isHomeOfficeIntegrationEnabled, homeOfficeApi);
    }

    @Test
    public void should_call_home_office_api_and_update_the_case() {

        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA)).thenReturn(java.util.Optional.of(homeOfficeCaseStatus));
        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(asylumCase.read(CONTACT_PREFERENCE)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.EA));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
        verify(asylumCase, times(1)).write(
            IS_HOME_OFFICE_INTEGRATION_ENABLED, "Yes");
        verify(asylumCase, times(1)).read(CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).write(
            CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
        verify(asylumCase, times(1)).read(APPEAL_TYPE);
        verify(asylumCase, times(1)).write(
            APPEAL_TYPE_DESCRIPTION, AppealType.EA.getDescription());
        verify(asylumCase, times(1)).read(HOME_OFFICE_CASE_STATUS_DATA);
        verify(applicationStatus, times(1)).modifyListDataForCcd();
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_SUBMITTED);

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeCaseValidateHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && callback.getEvent() == SUBMIT_APPEAL
                    && callback.getCaseDetails().getState() == State.APPEAL_SUBMITTED
                ) {
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

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Test
    public void handler_throws_error_if_feature_not_enabled() {

        homeOfficeCaseValidateHandler = new HomeOfficeCaseValidateHandler(
            false,
            homeOfficeApi
        );

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}
