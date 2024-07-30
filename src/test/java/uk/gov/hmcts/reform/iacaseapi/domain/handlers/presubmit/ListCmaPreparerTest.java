package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ListCmaPreparerTest {

    private static final String MANCHESTER_ADDRESS = "Manchester Crown Court (Crown Square), "
                                                     + "Courts of Justice, Crown Square, M3 3FL";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationRefDataService locationRefDataService;

    private ListCmaPreparer listCmaPreparer;

    @BeforeEach
    public void setUp() {

        listCmaPreparer =
            new ListCmaPreparer(locationRefDataService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CMA);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_default_list_case_hearing_centre_field() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.MANCHESTER))
            .thenReturn(MANCHESTER_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, MANCHESTER_ADDRESS);
    }

    @Test
    void should_not_set_default_list_case_hearing_centre_if_case_hearing_centre_not_present() {

        when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    void should_set_error_when_requirements_not_reviewed() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).hasSize(1);
        Assertions.assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "You've made an invalid request. You cannot list the case management appointment until the hearing requirements have been reviewed.");

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, never()).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, never()).write(eq(LIST_CASE_HEARING_CENTRE_ADDRESS), anyString());
    }

    @Test
    void should_set_error_when_reviewed_requirements_flag_not_set() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).hasSize(1);
        Assertions.assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "You've made an invalid request. You cannot list the case management appointment until the hearing requirements have been reviewed.");

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, never()).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, never()).write(eq(LIST_CASE_HEARING_CENTRE_ADDRESS), anyString());
    }

    @Test
    void should_not_set_error_when_requirements_have_been_reviewed() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.MANCHESTER))
            .thenReturn(MANCHESTER_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, MANCHESTER_ADDRESS);
    }

    @Test
    void should_work_for_old_flow_when_requirements_not_captured() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.empty());
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.MANCHESTER))
            .thenReturn(MANCHESTER_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, MANCHESTER_ADDRESS);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = listCmaPreparer.canHandle(callbackStage, callback);

                if (event == Event.LIST_CMA
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

        assertThatThrownBy(() -> listCmaPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCmaPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCmaPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCmaPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
