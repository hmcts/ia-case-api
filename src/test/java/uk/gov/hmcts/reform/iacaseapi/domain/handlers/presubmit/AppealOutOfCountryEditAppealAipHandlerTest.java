package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AppealOutOfCountryEditAppealAipHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private final String sponsorEmail = "sponsor@email.com";
    private final String sponsorMobilePhone = "07123456789";
    private AppealOutOfCountryEditAppealAipHandler appealOutOfCountryEditAppealAipHandler;

    @BeforeEach
    public void setUp() {
        appealOutOfCountryEditAppealAipHandler = new AppealOutOfCountryEditAppealAipHandler(featureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_aip_fields_for_appellant_in_uk_change_to_out_of_country(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(APPEAL_TYPE);
        verify(asylumCase, times(1)).write(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.YES);
        verifyClearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_aip_fields_for_appellant_in_uk_change_to_in_country(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(APPEAL_TYPE);
        verify(asylumCase, times(1)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verifyClearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_not_clear_aip_fields_for_no_appellant_in_uk_change(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verifyUnclearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_aip_fields_for_an_appeal_type_change(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(APPEAL_TYPE_PREVIOUS_SELECTION, AppealType.class)).thenReturn(Optional.of(AppealType.RP));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(APPEAL_TYPE);
        verify(asylumCase, times(0)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1)).write(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.YES);
        verifyClearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_not_clear_aip_fields_when_no_change_to_appellant_in_uk_and_appeal_type_fields(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(APPEAL_TYPE_PREVIOUS_SELECTION, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1)).write(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.YES);
        verifyUnclearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_aip_fields_for_an_outside_uk_when_application_made_change(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(APPEAL_TYPE_PREVIOUS_SELECTION, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(APPELLANT_IN_UK);
        verify(asylumCase, times(0)).clear(APPEAL_TYPE);
        verify(asylumCase, times(0)).clear(OUTSIDE_UK_WHEN_APPLICATION_MADE);
        verifyClearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_not_clear_aip_fields_when_no_change_to_appellant_in_uk_and_appeal_type_and_outside_uk_when_application_made_change_fields(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(APPEAL_TYPE_PREVIOUS_SELECTION, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verifyUnclearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_clear_sponsor_fields_for_when_has_sponsor_changed_to_no(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(APPEAL_TYPE);
        verify(asylumCase, times(0)).clear(HAS_CORRESPONDENCE_ADDRESS);
        verify(asylumCase, times(1)).write(HAS_CORRESPONDENCE_ADDRESS, YesOrNo.YES);
        verify(asylumCase, times(0)).clear(HOME_OFFICE_REFERENCE_NUMBER);
        verify(asylumCase, times(0)).clear(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, times(0)).clear(DECISION_LETTER_RECEIVED_DATE);
        verify(asylumCase, times(0)).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        verify(asylumCase, times(0)).clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
        verify(asylumCase, times(0)).clear(GWF_REFERENCE_NUMBER);
        verify(asylumCase, times(0)).clear(OUTSIDE_UK_WHEN_APPLICATION_MADE);
        verify(asylumCase, times(0)).clear(APPELLANT_GIVEN_NAMES);
        verify(asylumCase, times(0)).clear(APPELLANT_FAMILY_NAME);
        verify(asylumCase, times(0)).clear(APPELLANT_DATE_OF_BIRTH);
        verify(asylumCase, times(0)).clear(APPELLANT_NATIONALITIES);
        verify(asylumCase, times(0)).clear(APPELLANT_NATIONALITIES_DESCRIPTION);
        verify(asylumCase, times(0)).clear(APPELLANT_STATELESS);
        verify(asylumCase, times(0)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(0)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(0)).clear(SEARCH_POSTCODE);
        verify(asylumCase, times(0)).clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(0)).clear(SUBSCRIPTIONS);
        verify(asylumCase, times(0)).clear(HAS_SPONSOR);
        verify(asylumCase, times(0)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verifyClearedSponsorFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_to_sponsor_email_and_sponsor_mobile_phone_fields_when_sponsor_subscriptions_available(Event event) {

        Subscriber subscriber = new Subscriber(
            SubscriberType.SUPPORTER,
            sponsorEmail,
            YesOrNo.YES,
            sponsorMobilePhone,
            YesOrNo.YES
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(SPONSOR_SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).write(SPONSOR_EMAIL, sponsorEmail);
        verify(asylumCase, times(1)).write(AIP_SPONSOR_EMAIL_FOR_DISPLAY, sponsorEmail);
        verify(asylumCase, times(1)).write(SPONSOR_MOBILE_NUMBER, sponsorMobilePhone);
        verify(asylumCase, times(1)).write(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY, sponsorMobilePhone);
        verifyUnclearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_to_sponsor_email_field_when_sponsor_email_subscription_available(Event event) {

        Subscriber subscriber = new Subscriber(
            SubscriberType.SUPPORTER,
            sponsorEmail,
            YesOrNo.YES,
            "",
            YesOrNo.NO
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(SPONSOR_SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(1)).write(SPONSOR_EMAIL, sponsorEmail);
        verify(asylumCase, times(1)).write(AIP_SPONSOR_EMAIL_FOR_DISPLAY, sponsorEmail);
        verify(asylumCase, times(0)).write(SPONSOR_MOBILE_NUMBER, sponsorMobilePhone);
        verify(asylumCase, times(0)).write(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY, sponsorMobilePhone);
        verifyUnclearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_to_sponsor_mobile_phone_field_when_sponsor_phone_subscription_available(Event event) {

        Subscriber subscriber = new Subscriber(
            SubscriberType.SUPPORTER,
            "",
            YesOrNo.NO,
            sponsorMobilePhone,
            YesOrNo.YES
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(SPONSOR_SUBSCRIPTIONS)).thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).read(APPELLANT_IN_UK, YesOrNo.class);
        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.YES);
        verify(asylumCase, times(0)).write(SPONSOR_EMAIL, sponsorEmail);
        verify(asylumCase, times(0)).write(AIP_SPONSOR_EMAIL_FOR_DISPLAY, sponsorEmail);
        verify(asylumCase, times(1)).write(SPONSOR_MOBILE_NUMBER, sponsorMobilePhone);
        verify(asylumCase, times(1)).write(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY, sponsorMobilePhone);
        verifyUnclearedFields(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_to_admin_sponsor_email_and_mobile_when_is_admin(Event event) {
        Subscriber subscriber = new Subscriber(
                SubscriberType.SUPPORTER,
                sponsorEmail,
                YesOrNo.YES,
                sponsorMobilePhone,
                YesOrNo.YES
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(SPONSOR_SUBSCRIPTIONS))
                .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("1", subscriber))));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(SPONSOR_EMAIL_ADMIN_J, sponsorEmail);
        verify(asylumCase, times(1)).write(SPONSOR_MOBILE_NUMBER_ADMIN_J, sponsorMobilePhone);

        verify(asylumCase, never()).write(SPONSOR_EMAIL, sponsorEmail);
        verify(asylumCase, never()).write(AIP_SPONSOR_EMAIL_FOR_DISPLAY, sponsorEmail);
        verify(asylumCase, never()).write(SPONSOR_MOBILE_NUMBER, sponsorMobilePhone);
        verify(asylumCase, never()).write(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY, sponsorMobilePhone);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_to_normal_sponsor_fields_when_not_admin(Event event) {
        Subscriber subscriber = new Subscriber(
                SubscriberType.SUPPORTER,
                sponsorEmail,
                YesOrNo.YES,
                sponsorMobilePhone,
                YesOrNo.YES
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(SPONSOR_SUBSCRIPTIONS))
                .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("1", subscriber))));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(SPONSOR_EMAIL, sponsorEmail);
        verify(asylumCase, times(1)).write(AIP_SPONSOR_EMAIL_FOR_DISPLAY, sponsorEmail);
        verify(asylumCase, times(1)).write(SPONSOR_MOBILE_NUMBER, sponsorMobilePhone);
        verify(asylumCase, times(1)).write(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY, sponsorMobilePhone);

        verify(asylumCase, never()).write(SPONSOR_EMAIL_ADMIN_J, sponsorEmail);
        verify(asylumCase, never()).write(SPONSOR_MOBILE_NUMBER_ADMIN_J, sponsorMobilePhone);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);

        assertThatThrownBy(
            () -> appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(featureToggler.getValue("aip-ooc-feature", false)).thenReturn(true);
                when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

                boolean canHandle = appealOutOfCountryEditAppealAipHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (event.equals(Event.EDIT_APPEAL) || event.equals(Event.EDIT_APPEAL_AFTER_SUBMIT))) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealOutOfCountryEditAppealAipHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> appealOutOfCountryEditAppealAipHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealOutOfCountryEditAppealAipHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> appealOutOfCountryEditAppealAipHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private void verifyClearedFields(AsylumCase asylumCase) {
        verify(asylumCase, times(1)).clear(DATE_CLIENT_LEAVE_UK);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, times(1)).clear(DECISION_LETTER_RECEIVED_DATE);
        verify(asylumCase, times(1)).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        verify(asylumCase, times(1)).clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
        verify(asylumCase, times(1)).clear(GWF_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(APPELLANT_GIVEN_NAMES);
        verify(asylumCase, times(1)).clear(APPELLANT_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(APPELLANT_DATE_OF_BIRTH);
        verify(asylumCase, times(1)).clear(APPELLANT_NATIONALITIES);
        verify(asylumCase, times(1)).clear(APPELLANT_NATIONALITIES_DESCRIPTION);
        verify(asylumCase, times(1)).clear(APPELLANT_STATELESS);
        verify(asylumCase, times(1)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(1)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(1)).clear(SEARCH_POSTCODE);
        verify(asylumCase, times(1)).clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(1)).clear(SUBSCRIPTIONS);
        verify(asylumCase, times(1)).clear(HAS_SPONSOR);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1)).clear(DECISION_HEARING_FEE_OPTION);
        verifyClearedSponsorFields(asylumCase);
    }

    private void verifyClearedSponsorFields(AsylumCase asylumCase) {
        verify(asylumCase, times(1)).clear(SPONSOR_GIVEN_NAMES);
        verify(asylumCase, times(1)).clear(SPONSOR_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(SPONSOR_NAME_FOR_DISPLAY);
        verify(asylumCase, times(1)).clear(SPONSOR_ADDRESS);
        verify(asylumCase, times(1)).clear(SPONSOR_ADDRESS_FOR_DISPLAY);
        verify(asylumCase, times(1)).clear(SPONSOR_CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(SPONSOR_SUBSCRIPTIONS);
        verify(asylumCase, times(1)).clear(SPONSOR_EMAIL);
        verify(asylumCase, times(1)).clear(AIP_SPONSOR_EMAIL_FOR_DISPLAY);
        verify(asylumCase, times(1)).clear(SPONSOR_MOBILE_NUMBER);
        verify(asylumCase, times(1)).clear(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY);
        verify(asylumCase, times(1)).clear(SPONSOR_AUTHORISATION);
        verify(asylumCase, times(1)).clear(SPONSOR_PARTY_ID);
    }

    private void verifyUnclearedFields(AsylumCase asylumCase) {
        verify(asylumCase, times(0)).clear(APPEAL_TYPE);
        verify(asylumCase, times(0)).clear(DATE_CLIENT_LEAVE_UK);
        verify(asylumCase, times(0)).clear(HOME_OFFICE_REFERENCE_NUMBER);
        verify(asylumCase, times(0)).clear(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, times(0)).clear(DECISION_LETTER_RECEIVED_DATE);
        verify(asylumCase, times(0)).clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        verify(asylumCase, times(0)).clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
        verify(asylumCase, times(0)).clear(GWF_REFERENCE_NUMBER);
        verify(asylumCase, times(0)).clear(OUTSIDE_UK_WHEN_APPLICATION_MADE);
        verify(asylumCase, times(0)).clear(APPELLANT_GIVEN_NAMES);
        verify(asylumCase, times(0)).clear(APPELLANT_FAMILY_NAME);
        verify(asylumCase, times(0)).clear(APPELLANT_DATE_OF_BIRTH);
        verify(asylumCase, times(0)).clear(APPELLANT_NATIONALITIES);
        verify(asylumCase, times(0)).clear(APPELLANT_NATIONALITIES_DESCRIPTION);
        verify(asylumCase, times(0)).clear(APPELLANT_STATELESS);
        verify(asylumCase, times(0)).clear(APPELLANT_HAS_FIXED_ADDRESS);
        verify(asylumCase, times(0)).clear(APPELLANT_ADDRESS);
        verify(asylumCase, times(0)).clear(SEARCH_POSTCODE);
        verify(asylumCase, times(0)).clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
        verify(asylumCase, times(0)).clear(SUBSCRIPTIONS);
        verify(asylumCase, times(0)).clear(HAS_SPONSOR);
        verify(asylumCase, times(0)).clear(SPONSOR_GIVEN_NAMES);
        verify(asylumCase, times(0)).clear(SPONSOR_FAMILY_NAME);
        verify(asylumCase, times(0)).clear(SPONSOR_NAME_FOR_DISPLAY);
        verify(asylumCase, times(0)).clear(SPONSOR_ADDRESS);
        verify(asylumCase, times(0)).clear(SPONSOR_ADDRESS_FOR_DISPLAY);
        verify(asylumCase, times(0)).clear(SPONSOR_SUBSCRIPTIONS);
        verify(asylumCase, times(0)).clear(SPONSOR_CONTACT_PREFERENCE);
        verify(asylumCase, times(0)).clear(SPONSOR_EMAIL);
        verify(asylumCase, times(0)).clear(AIP_SPONSOR_EMAIL_FOR_DISPLAY);
        verify(asylumCase, times(0)).clear(SPONSOR_MOBILE_NUMBER);
        verify(asylumCase, times(0)).clear(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY);
        verify(asylumCase, times(0)).clear(SPONSOR_AUTHORISATION);
        verify(asylumCase, times(0)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
    }
}
