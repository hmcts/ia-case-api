package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.ApplicationStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HomeOfficeCaseValidateHandlerTest {

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
    @Mock
    private FeatureToggler featureToggler;

    private boolean isHomeOfficeIntegrationEnabled = true;

    private HomeOfficeCaseValidateHandler homeOfficeCaseValidateHandler;

    @BeforeEach
    public void setUp() {

        homeOfficeCaseValidateHandler =
            new HomeOfficeCaseValidateHandler(featureToggler, isHomeOfficeIntegrationEnabled, homeOfficeApi);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void handle_should_return_if_appeal_type_is_not_present() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("AppealType is not present.");
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA)).thenReturn(Optional.of(homeOfficeCaseStatus));
        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(asylumCase.read(CONTACT_PREFERENCE)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        List<IdValue<NationalityFieldValue>> nlist = new ArrayList<>();
        nlist.add(new IdValue<>("0", new NationalityFieldValue("IS")));
        nlist.add(new IdValue<>("1", new NationalityFieldValue("CA")));
        nlist.add(new IdValue<>("2", new NationalityFieldValue("VA")));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nlist));
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(
            IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).read(CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).write(
            CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
        verify(asylumCase, times(1)).write(
            APPEAL_TYPE_DESCRIPTION, appealType.getDescription());
        verify(asylumCase, times(1)).write(
            APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
        verify(asylumCase, times(1)).read(HOME_OFFICE_CASE_STATUS_DATA);
        verify(asylumCase, times(1)).write(
            HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
        verify(applicationStatus, times(1)).modifyListDataForCcd();
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_for_detained_appeals(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA)).thenReturn(Optional.of(homeOfficeCaseStatus));
        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(asylumCase.read(CONTACT_PREFERENCE)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        List<IdValue<NationalityFieldValue>> nlist = new ArrayList<>();
        nlist.add(new IdValue<>("0", new NationalityFieldValue("IS")));
        nlist.add(new IdValue<>("1", new NationalityFieldValue("CA")));
        nlist.add(new IdValue<>("2", new NationalityFieldValue("VA")));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nlist));
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(1)).write(
            IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        verify(asylumCase, times(1)).read(CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).write(
            CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
        verify(asylumCase, times(1)).write(
            APPEAL_TYPE_DESCRIPTION, appealType.getDescription());
        verify(asylumCase, times(1)).write(
            APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
        verify(asylumCase, times(1)).read(HOME_OFFICE_CASE_STATUS_DATA);
        verify(asylumCase, times(1)).write(
            HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
        verify(applicationStatus, times(1)).modifyListDataForCcd();
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_not_call_home_office_api_for_ejp_appeals(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_not_call_home_office_api_when_isNotificationTurnedOff_yes(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_not_call_home_office_api_for_aaa_appeals(Event event) {

        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AG));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(
            IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        verify(asylumCase, times(0)).read(CONTACT_PREFERENCE);
        verify(asylumCase, times(0)).write(
            CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
        verify(asylumCase, times(0)).write(
            APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
        verify(asylumCase, times(0)).read(HOME_OFFICE_CASE_STATUS_DATA);
        verify(asylumCase, times(0)).write(
            HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
        verify(applicationStatus, times(0)).modifyListDataForCcd();
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_pa_rp_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA)).thenReturn(Optional.of(homeOfficeCaseStatus));
        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(asylumCase.read(CONTACT_PREFERENCE)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        List<IdValue<NationalityFieldValue>> nlist = new ArrayList<>();
        nlist.add(new IdValue<>("0", new NationalityFieldValue("IS")));
        nlist.add(new IdValue<>("1", new NationalityFieldValue("CA")));
        nlist.add(new IdValue<>("2", new NationalityFieldValue("VA")));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nlist));
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(PA, RP).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);

            verify(asylumCase, times(1)).read(CONTACT_PREFERENCE);
            verify(asylumCase, times(1)).write(
                    CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
            verify(asylumCase, times(1)).write(
                    APPEAL_TYPE_DESCRIPTION, appealType.getDescription());
            verify(asylumCase, times(1)).write(
                    APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
            verify(asylumCase, times(1)).read(HOME_OFFICE_CASE_STATUS_DATA);
            verify(asylumCase, times(1)).write(
                    HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
            verify(applicationStatus, times(1)).modifyListDataForCcd();
        } else {

            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
            verify(asylumCase, times(0)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);

            verify(asylumCase, times(0)).read(CONTACT_PREFERENCE);
            verify(asylumCase, times(0)).write(
                    CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
            verify(asylumCase, times(0)).write(
                    APPEAL_TYPE_DESCRIPTION, appealType.getDescription());
            verify(asylumCase, times(0)).write(
                    APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
            verify(asylumCase, times(0)).read(HOME_OFFICE_CASE_STATUS_DATA);
            verify(asylumCase, times(0)).write(
                    HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
            verify(applicationStatus, times(0)).modifyListDataForCcd();
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_dc_ea_hu_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA)).thenReturn(Optional.of(homeOfficeCaseStatus));
        when(homeOfficeCaseStatus.getApplicationStatus()).thenReturn(applicationStatus);
        when(asylumCase.read(CONTACT_PREFERENCE)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        List<IdValue<NationalityFieldValue>> nlist = new ArrayList<>();
        nlist.add(new IdValue<>("0", new NationalityFieldValue("IS")));
        nlist.add(new IdValue<>("1", new NationalityFieldValue("CA")));
        nlist.add(new IdValue<>("2", new NationalityFieldValue("VA")));

        when(asylumCase.read(APPELLANT_NATIONALITIES)).thenReturn(Optional.of(nlist));
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(DC, EA, HU).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);

            verify(asylumCase, times(1)).read(CONTACT_PREFERENCE);
            verify(asylumCase, times(1)).write(
                    CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
            verify(asylumCase, times(1)).write(
                    APPEAL_TYPE_DESCRIPTION, appealType.getDescription());
            verify(asylumCase, times(1)).write(
                    APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
            verify(asylumCase, times(1)).read(HOME_OFFICE_CASE_STATUS_DATA);
            verify(asylumCase, times(1)).write(
                    HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
            verify(applicationStatus, times(1)).modifyListDataForCcd();
        } else {

            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
            verify(asylumCase, times(0)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);

            verify(asylumCase, times(0)).read(CONTACT_PREFERENCE);
            verify(asylumCase, times(0)).write(
                    CONTACT_PREFERENCE_DESCRIPTION, ContactPreference.WANTS_EMAIL.getDescription());
            verify(asylumCase, times(0)).write(
                    APPEAL_TYPE_DESCRIPTION, appealType.getDescription());
            verify(asylumCase, times(0)).write(
                    APPELLANT_NATIONALITIES_DESCRIPTION, "Iceland<br />Canada<br />Holy See (Vatican City State)");
            verify(asylumCase, times(0)).read(HOME_OFFICE_CASE_STATUS_DATA);
            verify(asylumCase, times(0)).write(
                    HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.YES);
            verify(applicationStatus, times(0)).modifyListDataForCcd();
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_not_call_home_office_api_when_ooc_and_human_rights_decision_is_chosen(Event event, AppealType appealType) {

        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK,YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(any(),any());
    }

    private static Stream<Arguments> eventAndAppealTypesData() {

        return Stream.of(
                Arguments.of(SUBMIT_APPEAL, PA),
                Arguments.of(SUBMIT_APPEAL, RP),
                Arguments.of(SUBMIT_APPEAL, DC),
                Arguments.of(SUBMIT_APPEAL, EA),
                Arguments.of(SUBMIT_APPEAL, HU),
                Arguments.of(MARK_APPEAL_PAID, PA),
                Arguments.of(MARK_APPEAL_PAID, RP),
                Arguments.of(MARK_APPEAL_PAID, DC),
                Arguments.of(MARK_APPEAL_PAID, EA),
                Arguments.of(MARK_APPEAL_PAID, HU),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, PA),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, RP),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, DC),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, EA),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, HU)
        );
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeCaseValidateHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == SUBMIT_APPEAL
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
    void should_not_allow_null_arguments() {

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
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Test
    void handler_throws_error_if_feature_not_enabled() {

        homeOfficeCaseValidateHandler = new HomeOfficeCaseValidateHandler(
            featureToggler,
            false,
            homeOfficeApi
        );

        assertThatThrownBy(() -> homeOfficeCaseValidateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}
