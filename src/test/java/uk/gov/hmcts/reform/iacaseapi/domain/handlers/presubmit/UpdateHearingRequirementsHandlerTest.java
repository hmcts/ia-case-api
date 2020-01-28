package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class UpdateHearingRequirementsHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Captor private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "30/01/2019";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "31/01/2019";
    private String applicationStatus = "In progress";

    private List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
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

    private UpdateHearingRequirementsHandler updateHearingRequirementsHandler;

    @Before
    public void setUp() {
        updateHearingRequirementsHandler = new UpdateHearingRequirementsHandler();

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
    }

    @Test
    public void should_set_witness_count_to_zero_and_overview_page_flags() {

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

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    public void should_set_witness_count_and_overview_page_flags() {

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

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

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