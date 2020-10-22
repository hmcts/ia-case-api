package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unchecked")
public class HomeOfficeCaseNotificationsHandlerTest {

    @Mock private HomeOfficeApi<AsylumCase> homeOfficeApi;
    @Mock private Callback<AsylumCase> callback;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;

    @InjectMocks
    private HomeOfficeCaseNotificationsHandler homeOfficeCaseNotificationsHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        homeOfficeCaseNotificationsHandler =
            new HomeOfficeCaseNotificationsHandler(featureToggler, homeOfficeApi);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
    }

    @Test
    public void should_call_home_office_api_and_update_the_case_for_respondent_evidence() {

        when(callback.getEvent()).thenReturn(REQUEST_RESPONDENT_EVIDENCE);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
    }

    @Test
    public void should_call_home_office_api_and_update_the_case_for_respondent_review() {

        when(callback.getEvent()).thenReturn(REQUEST_RESPONDENT_REVIEW);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
    }

    @Test
    @Parameters({
        "LIST_CASE",
        "EDIT_CASE_LISTING",
        "ADJOURN_HEARING_WITHOUT_DATE"
    })
    public void should_call_home_office_api_and_update_the_case_for_list_case(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeCaseNotificationsHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && Arrays.asList(
                        Event.REQUEST_RESPONDENT_EVIDENCE,
                        Event.REQUEST_RESPONDENT_REVIEW,
                        Event.LIST_CASE,
                        Event.EDIT_CASE_LISTING,
                        Event.ADJOURN_HEARING_WITHOUT_DATE
                    ).contains(callback.getEvent())
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

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handler_throws_error_if_feature_not_enabled() {

        homeOfficeCaseNotificationsHandler = new HomeOfficeCaseNotificationsHandler(
            featureToggler,
            homeOfficeApi
        );

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
