package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MIGRATION_HMC_SECOND_PART_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MIGRATION_MAIN_TEXT_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MigrateAriaCasesDocumentUploaderMidEventTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentWithDescription respondentEvidence1;
    private MigrateAriaCasesDocumentUploaderMidEvent migrateAriaCasesDocumentUploaderMidEvent;

    @BeforeEach
    public void setUp() {
        migrateAriaCasesDocumentUploaderMidEvent = new MigrateAriaCasesDocumentUploaderMidEvent();
        when(callback.getPageId()).thenReturn("migrateAriaCasesDocumentUploaderMidEvent");
        when(callback.getEvent()).thenReturn(Event.PROGRESS_MIGRATED_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn("migrateAriaCasesDocumentUploaderMidEvent");

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = migrateAriaCasesDocumentUploaderMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.PROGRESS_MIGRATED_CASE)
                    && callbackStage == MID_EVENT) {
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

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderMidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderMidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderMidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "APPEAL_SUBMITTED", "AWAITING_RESPONDENT_EVIDENCE", "CASE_UNDER_REVIEW", "REASONS_FOR_APPEAL_SUBMITTED", "LISTING", "DECISION", "FTPA_SUBMITTED"
    })
    void should_successfully_set_migration_text_flags_for_VHH_states(State state) {
        List<IdValue<DocumentWithDescription>> respondentEvidence =
            Arrays.asList(
                new IdValue<>("1", respondentEvidence1)
            );

        when(asylumCase.read(ARIA_DESIRED_STATE, State.class))
            .thenReturn(Optional.of(state));
        when(asylumCase.read(RESPONDENT_EVIDENCE)).thenReturn(Optional.of(respondentEvidence));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            migrateAriaCasesDocumentUploaderMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        AsylumCase asylumCaseResponse = callbackResponse.getData();
        verify(asylumCaseResponse).write(MIGRATION_MAIN_TEXT_VISIBLE, "VHH");


    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING", "DECIDED"
    })
    void should_successfully_set_migration_text_flags_for_HMC_states(State state) {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class))
            .thenReturn(Optional.of(state));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class)).thenReturn(Optional.of(UpdateTribunalRules.UNDER_RULE_31));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            migrateAriaCasesDocumentUploaderMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        AsylumCase asylumCaseResponse = callbackResponse.getData();
        verify(asylumCaseResponse).write(MIGRATION_MAIN_TEXT_VISIBLE, "HMC");
        verify(asylumCaseResponse).write(MIGRATION_HMC_SECOND_PART_VISIBLE, "Yes");
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "AWAITING_RESPONDENT_EVIDENCE"
    })
    void should_successfully_set_migration_text_flags_for_MoveIT_states(State state) {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(state));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            migrateAriaCasesDocumentUploaderMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        AsylumCase asylumCaseResponse = callbackResponse.getData();
        verify(asylumCaseResponse).write(MIGRATION_MAIN_TEXT_VISIBLE, "MoveIT");
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "DECIDED", "FTPA_DECIDED", "ENDED",
    })
    void should_successfully_set_migration_text_flags_for_VHHToCCD_states(State state) {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(state));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            migrateAriaCasesDocumentUploaderMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        AsylumCase asylumCaseResponse = callbackResponse.getData();
        verify(asylumCaseResponse).write(MIGRATION_MAIN_TEXT_VISIBLE, "VHHToCCD");
    }

    @Test
    void should_throw_if_aria_desired_not_present() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("ariaDesiredState is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
