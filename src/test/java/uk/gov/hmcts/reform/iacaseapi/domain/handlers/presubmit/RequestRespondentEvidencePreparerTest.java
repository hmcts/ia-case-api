package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestRespondentEvidencePreparerTest {

    private static final int DUE_IN_DAYS = 14;

    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<String> asylumCaseValuesCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestRespondentEvidencePreparer requestRespondentEvidencePreparer;

    @BeforeEach
    public void setUp() {
        requestRespondentEvidencePreparer =
            new RequestRespondentEvidencePreparer(DUE_IN_DAYS, featureToggler, dateProvider);
    }

    @Test
    void handler_should_return_error_if_appeal_type_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("AppealType is not present.");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void should_prepare_send_direction_fields(AppealType appealType) {

        final String expectedExplanationContains = "A notice of appeal has been lodged against this decision.";
        final Parties expectedParties = Parties.RESPONDENT;
        final String expectedDateDue = "2018-12-07";

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(4)).write(asylumExtractorCaptor.capture(), asylumCaseValuesCaptor.capture());

        List<AsylumCaseFieldDefinition> extractors = asylumExtractorCaptor.getAllValues();
        List<String> asylumCaseValues = asylumCaseValuesCaptor.getAllValues();

        assertThat(
            asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
            .contains("You have until the date indicated below to supply");

        assertThat(
            asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
            .contains(expectedExplanationContains);

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDateDue);
        verify(asylumCase, times(1)).write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handle_should_not_error_for_out_of_country_appeals(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handle_should_return_callback_response_for_no_record_out_of_time_decision(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handle_should_throw_error_for_recorded_out_of_time_decision_and_missing_decision_type(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Out of time decision type is not present");
    }

    @ParameterizedTest
    @ValueSource(strings = { "FAIL", "MULTIPLE" })
    void handle_should_throw_error_for_the_failed_home_office_response_pa_appeal_type(String hoSearchStatus) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(PA));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of(hoSearchStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(!callbackResponse.getErrors().isEmpty());
        assertThat(callbackResponse.getErrors())
                .contains("You need to match the appellant details before you can request the respondent evidence.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "FAIL", "MULTIPLE" })
    void handle_should_throw_error_for_the_failed_home_office_response_hu_appeal_type(String hoSearchStatus) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(HU));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of(hoSearchStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(!callbackResponse.getErrors().isEmpty());
        assertThat(callbackResponse.getErrors())
                .contains("You need to match the appellant details before you can request the respondent evidence.");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handle_should_return_callback_error_for_rejected_record_out_of_time_decision(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class))
            .thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("Record out of time decision is rejected. The appeal must be ended.");
    }


    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestRespondentEvidencePreparer.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_RESPONDENT_EVIDENCE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

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

        assertThatThrownBy(() -> requestRespondentEvidencePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> requestRespondentEvidencePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentEvidencePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentEvidencePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
