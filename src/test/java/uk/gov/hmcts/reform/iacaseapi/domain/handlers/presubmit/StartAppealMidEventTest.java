package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class StartAppealMidEventTest {

    private static final String HOME_OFFICE_DECISION_PAGE_ID = "homeOfficeDecision";
    private static final String OUT_OF_COUNTRY_PAGE_ID = "outOfCountry";
    private static final String DETENTION_FACILITY_PAGE_ID = "detentionFacility";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private String correctHomeOfficeReferenceFormatCid = "01234567";
    private String correctHomeOfficeReferenceFormatUan = "1234-5678-9876-5432";
    private String wrongHomeOfficeReferenceFormat = "A234567";
    private String callbackErrorMessage =
        "Enter the Home office reference or Case ID in the correct format. The Home office reference or Case ID cannot include letters and must be either 9 digits or 16 digits with dashes.";
    private String getCallbackErrorMessageOutOfCountry = "This option is currently unavailable";
    private StartAppealMidEvent startAppealMidEvent;

    @BeforeEach
    public void setUp() {
        startAppealMidEvent = new StartAppealMidEvent();

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(HOME_OFFICE_DECISION_PAGE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {HOME_OFFICE_DECISION_PAGE_ID, OUT_OF_COUNTRY_PAGE_ID, DETENTION_FACILITY_PAGE_ID, ""})
    void it_can_handle_callback(String pageId) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(pageId);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = startAppealMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL || event == Event.EDIT_APPEAL_AFTER_SUBMIT)
                    && callbackStage == MID_EVENT
                    && (callback.getPageId().equals(DETENTION_FACILITY_PAGE_ID)
                        || callback.getPageId().equals(HOME_OFFICE_DECISION_PAGE_ID)
                        || callback.getPageId().equals(OUT_OF_COUNTRY_PAGE_ID))) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> startAppealMidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> startAppealMidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> startAppealMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> startAppealMidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_error_when_home_office_reference_format_is_wrong() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(wrongHomeOfficeReferenceFormat));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(callbackErrorMessage);
    }

    @Test
    void should_error_when_trying_to_select_out_of_country_path_for_internal_case_creation() {
        when(callback.getPageId()).thenReturn(OUT_OF_COUNTRY_PAGE_ID);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(getCallbackErrorMessageOutOfCountry);
    }

    @Test
    void should_successfully_validate_when_home_office_reference_format_is_correct_cid() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(correctHomeOfficeReferenceFormatCid));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_successfully_validate_when_home_office_reference_format_is_correct_uan() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(correctHomeOfficeReferenceFormatUan));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_not_touch_home_office_reference_numbers_when_ooc_and_refusal_of_human_rights_is_decided() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(any(),any());
    }

    @ParameterizedTest
    @EnumSource(DetentionFacility.class)
    void should_set_is_accelerated_detained_value_to_no_if_prison_or_other_detention_facility(DetentionFacility detentionFacility) {
        when(callback.getPageId()).thenReturn(DETENTION_FACILITY_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(DetentionFacility.PRISON.toString(), DetentionFacility.PRISON.toString()).contains(detentionFacility)) {
            assertEquals(YesOrNo.NO, callbackResponse.getData().get("isAcceleratedDetainedAppeal"));
        }
    }
}
