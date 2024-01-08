package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ReviewDraftHearingRequirementsPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;
    @Mock
    private List<IdValue<InterpreterLanguage>> interpreterLanguage;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Captor
    private ArgumentCaptor<YesOrNo> autoHearingRequestEnabledCaptor;
    @Captor
    private ArgumentCaptor<DynamicList> hearingLocationsCaptor;

    private ReviewDraftHearingRequirementsPreparer reviewDraftHearingRequirementsPreparer;

    @BeforeEach
    public void setUp() {
        reviewDraftHearingRequirementsPreparer =
            new ReviewDraftHearingRequirementsPreparer(locationBasedFeatureToggler, locationRefDataService);

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_review_hearing_requirements() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        witnessDetails = Arrays.asList(
            new IdValue<>("1", new WitnessDetails("Witness1Given", "Witness1Family")),
            new IdValue<>("2", new WitnessDetails("Witness2Given"))
        );

        InterpreterLanguage interpreterLanguageObject = new InterpreterLanguage();
        interpreterLanguageObject.setLanguage("Irish");
        interpreterLanguageObject.setLanguageDialect("N/A");
        interpreterLanguage = Arrays.asList(
            new IdValue<>("1", interpreterLanguageObject)
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(WITNESS_DETAILS);
        verify(asylumCase, times(1)).read(INTERPRETER_LANGUAGE);

        verify(asylumCase, times(1)).write(
            WITNESS_DETAILS_READONLY,
            "Name\t\tWitness1Given Witness1Family\nName\t\tWitness2Given");
        verify(asylumCase, times(1)).write(
            INTERPRETER_LANGUAGE_READONLY,
            "Language\t\tIrish\nDialect\t\t\tN/A\n");
    }

    @Test
    void should_set_outside_evidence_out_of_country_default_for_old_cases() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC)).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
            IS_EVIDENCE_FROM_OUTSIDE_UK_OOC,
            YesOrNo.NO);

        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
    }

    @Test
    void should_set_outside_evidence_in_country_default_for_old_cases() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY)).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
            IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY,
            YesOrNo.NO);
    }

    @ParameterizedTest
    @CsvSource({
        "HU, 120",
        "EA, 120",
        "EU, 120",
        "DC, 180",
        "PA, 180",
        "RP, 180",
    })
    void should_set_default_length_hearing_for_appeal_type(AppealType appealType, String expectedResult) {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
                LIST_CASE_HEARING_LENGTH,
                expectedResult);
    }

    @Test
    void should_return_error_on_review_flag_is_missing() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(callbackResponse.getErrors().iterator().next(),
            "The case is already listed, you can't request hearing requirements");

        verify(asylumCase, times(0)).read(WITNESS_DETAILS);
        verify(asylumCase, times(0)).read(INTERPRETER_LANGUAGE);
    }

    @Test
    void should_set_auto_hearing_request_enabled_to_yes() {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.YES);
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(AUTO_HEARING_REQUEST_ENABLED), autoHearingRequestEnabledCaptor.capture());
        assertEquals(YesOrNo.YES, autoHearingRequestEnabledCaptor.getValue());
    }

    @Test
    void should_not_set_auto_hearing_request_enabled() {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.NO);
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never())
            .write(eq(AUTO_HEARING_REQUEST_ENABLED), autoHearingRequestEnabledCaptor.capture());
    }

    @Test
    void should_set_hearing_location_dynamic_list() {
        DynamicList hearingLocations =
            new DynamicList(new Value("", ""), List.of(new Value("key1", "value1")));
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.YES);
        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(hearingLocations);
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(HEARING_LOCATION), hearingLocationsCaptor.capture());
        DynamicList actual = hearingLocationsCaptor.getValue();
        assertEquals("key1", actual.getListItems().get(0).getCode());
        assertEquals("value1", actual.getListItems().get(0).getLabel());
    }

    @Test
    void should_not_set_hearing_location_dynamic_list() {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.NO);
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never())
            .write(eq(HEARING_LOCATION), any());
    }

    @Test
    void should_return_error_on_reviewing_hearing_requirements_twice() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've made an invalid request. The hearing requirements have already been reviewed.",
            callbackResponse.getErrors().iterator().next());

        verify(asylumCase, times(0)).read(WITNESS_DETAILS);
        verify(asylumCase, times(0)).read(INTERPRETER_LANGUAGE);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
