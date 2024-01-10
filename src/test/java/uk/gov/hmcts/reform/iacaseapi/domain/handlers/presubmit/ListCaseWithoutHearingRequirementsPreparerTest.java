package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_HEARING_REQUEST_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ListCaseWithoutHearingRequirementsPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Captor
    private ArgumentCaptor<YesOrNo> autoHearingRequestEnabledCaptor;
    @Captor
    private ArgumentCaptor<DynamicList> hearingLocationsCaptor;

    private ListCaseWithoutHearingRequirementsPreparer listCaseWithoutHearingRequirementsPreparer;

    @BeforeEach
    public void setUp() {
        listCaseWithoutHearingRequirementsPreparer =
            new ListCaseWithoutHearingRequirementsPreparer(locationBasedFeatureToggler, locationRefDataService);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsPreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsPreparer.canHandle(ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsPreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
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
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
                LIST_CASE_HEARING_LENGTH,
                expectedResult);
    }

    @Test
    void should_set_auto_hearing_request_enabled_to_yes() {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.YES);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(AUTO_HEARING_REQUEST_ENABLED), autoHearingRequestEnabledCaptor.capture());
        assertEquals(YesOrNo.YES, autoHearingRequestEnabledCaptor.getValue());
    }

    @Test
    void should_not_set_auto_hearing_request_enabled() {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.NO);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCaseWithoutHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never())
            .write(eq(HEARING_LOCATION), any());
    }
}
