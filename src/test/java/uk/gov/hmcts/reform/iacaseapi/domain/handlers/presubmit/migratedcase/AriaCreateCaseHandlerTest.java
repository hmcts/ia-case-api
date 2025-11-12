package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE_SELECTED_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_MIGRATION_TASK_DUE_DAYS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ARIA_MIGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ARIA_MIGRATED_FILTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AriaCreateCaseHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    private AriaCreateCaseHandler ariaCreateCaseHandler;

    private final String nextAppealReferenceNumber = "EXAMPLE/10000/2024";
    private final String ariaTaskDueDays = "10";
    private final LocalDate now = LocalDate.now();

    @BeforeEach
    public void setUp() {

        ariaCreateCaseHandler =
            new AriaCreateCaseHandler(dateProvider, appealReferenceNumberGenerator);
        when(callback.getEvent()).thenReturn(Event.ARIA_CREATE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(ARIA_MIGRATION_TASK_DUE_DAYS, String.class)).thenReturn(Optional.of(ariaTaskDueDays));
    }

    @Test
    void set_to_earliest() {
        assertThat(ariaCreateCaseHandler.getDispatchPriority()).isEqualTo(LATEST);
    }

    @Test
    void should_set_next_appeal_reference_number_if_not_present_for_submit_appeal() {
        when(appealReferenceNumberGenerator.generate(123, AppealType.PA))
            .thenReturn(nextAppealReferenceNumber);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(State.LISTING));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ariaCreateCaseHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, nextAppealReferenceNumber);
        verify(asylumCase, times(1)).write(APPEAL_SUBMISSION_DATE, now.toString());
        verify(asylumCase, times(1)).write(IS_ARIA_MIGRATED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(IS_ARIA_MIGRATED_FILTER, YesOrNo.YES);
        verify(asylumCase, times(1)).write(ARIA_DESIRED_STATE_SELECTED_VALUE, "Listing");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ariaCreateCaseHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT && event == Event.ARIA_CREATE_CASE) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_return_error_when_aria_migration_task_due_day_missing() {
        when(asylumCase.read(ARIA_MIGRATION_TASK_DUE_DAYS, String.class)).thenReturn(Optional.empty());
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                ariaCreateCaseHandler.handle(ABOUT_TO_SUBMIT, callback);
        assertThat(callbackResponse.getErrors())
                .containsExactly(
                        "You must provide ariaMigrationTaskDueDays as part of the case creation.");

        when(asylumCase.read(ARIA_MIGRATION_TASK_DUE_DAYS, String.class)).thenReturn(Optional.of(" "));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse2 =
                ariaCreateCaseHandler.handle(ABOUT_TO_SUBMIT, callback);
        assertThat(callbackResponse2.getErrors())
                .containsExactly(
                        "You must provide ariaMigrationTaskDueDays as part of the case creation.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ariaCreateCaseHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ariaCreateCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ariaCreateCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ariaCreateCaseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ariaCreateCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
