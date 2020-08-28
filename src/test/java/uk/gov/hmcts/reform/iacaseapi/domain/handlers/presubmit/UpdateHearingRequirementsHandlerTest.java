package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateHearingRequirementsHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Captor ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    String applicationSupplier = "Legal representative";
    String applicationReason = "applicationReason";
    String applicationDate = "30/01/2019";
    String applicationDecision = "Granted";
    String applicationDecisionReason = "Granted";
    String applicationDateOfDecision = "31/01/2019";
    String applicationStatus = "In progress";

    List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
        Collections.emptyList(),
        applicationSupplier,
        ApplicationType.UPDATE_HEARING_REQUIREMENTS.toString(),
        applicationReason,
        applicationDate,
        applicationDecision,
        applicationDecisionReason,
        applicationDateOfDecision,
        applicationStatus
    )));

    UpdateHearingRequirementsHandler updateHearingRequirementsHandler;

    @BeforeEach
    void setUp() {

        updateHearingRequirementsHandler = new UpdateHearingRequirementsHandler();
    }

    @Test
    void should_set_witness_count_to_zero_and_overview_page_flags() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(WITNESS_DETAILS);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.WITNESS_COUNT), eq(0));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE), eq(YesOrNo.YES));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.UNKNOWN));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.UPDATE_HEARING_REQUIREMENTS_EXISTS), eq(YesOrNo.YES));

        verify(asylumCase).clear(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS);
        // review fields should be cleared
        verify(asylumCase).clear(REVIEWED_HEARING_REQUIREMENTS);
        verify(asylumCase).clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(ADDITIONAL_TRIBUNAL_RESPONSE);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_witness_count_and_overview_page_flags() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(Arrays.asList(new IdValue("1", new WitnessDetails("cap")), new IdValue("2", new WitnessDetails("Pan")))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(WITNESS_DETAILS);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.WITNESS_COUNT), eq(2));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE), eq(YesOrNo.YES));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.UNKNOWN));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.UPDATE_HEARING_REQUIREMENTS_EXISTS), eq(YesOrNo.YES));

        verify(asylumCase).clear(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS);
        // review fields should be cleared
        verify(asylumCase).clear(REVIEWED_HEARING_REQUIREMENTS);
        verify(asylumCase).clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(ADDITIONAL_TRIBUNAL_RESPONSE);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_HEARING_REQUIREMENTS && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> updateHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
