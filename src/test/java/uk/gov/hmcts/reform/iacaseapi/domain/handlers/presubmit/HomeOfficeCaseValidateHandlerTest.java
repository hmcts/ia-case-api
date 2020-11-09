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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HOME_OFFICE_INTEGRATION_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
    public void should_be_handled_at_earliest_point() {
        assertEquals(DispatchPriority.EARLIEST, homeOfficeCaseValidateHandler.getDispatchPriority());
    }

    @Test
    public void should_call_home_office_api_and_update_the_case() {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA)).thenReturn(Optional.of(homeOfficeCaseStatus));
        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(asylumCase.read(CONTACT_PREFERENCE)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.EA));
        List<IdValue<NationalityFieldValue>> nlist = new ArrayList<>();
        nlist.add(new IdValue<>("0", new NationalityFieldValue("IS")));
        nlist.add(new IdValue<>("1", new NationalityFieldValue("CA")));
        nlist.add(new IdValue<>("2", new NationalityFieldValue("VA")));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nlist));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
        verify(asylumCase, times(1)).write(
            IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).read(CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).write(
            CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
        verify(asylumCase, times(1)).read(APPEAL_TYPE);
        verify(asylumCase, times(1)).write(
            APPEAL_TYPE_DESCRIPTION, AppealType.EA.getDescription());
        verify(asylumCase, times(1)).write(
            APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
        verify(asylumCase, times(1)).read(HOME_OFFICE_CASE_STATUS_DATA);
        verify(applicationStatus, times(1)).modifyListDataForCcd();
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeCaseValidateHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == SUBMIT_APPEAL
                    || callback.getEvent() == PAY_AND_SUBMIT_APPEAL
                    || callback.getEvent() == MARK_APPEAL_PAID
                    || callback.getEvent() == REQUEST_HOME_OFFICE_DATA)
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
