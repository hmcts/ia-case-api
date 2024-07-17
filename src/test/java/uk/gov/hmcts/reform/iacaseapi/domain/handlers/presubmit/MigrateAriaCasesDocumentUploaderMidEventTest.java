package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import net.bytebuddy.implementation.bytecode.Throw;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MIGRATION_MAIN_TEXT_VISIBLE;
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
    void should_successfully_validate_when_list_case_hearing_centre_is_not_decision_without_hearing_for_edit_case_listing(State state) {

        when(asylumCase.read(ARIA_DESIRED_STATE, State.class))
                .thenReturn(Optional.of(state));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            migrateAriaCasesDocumentUploaderMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertNotNull(callback);
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();

        AsylumCase asylumCaseResponse = callbackResponse.getData();



        switch (state) {
            case APPEAL_SUBMITTED, AWAITING_RESPONDENT_EVIDENCE, CASE_UNDER_REVIEW, REASONS_FOR_APPEAL_SUBMITTED, LISTING, DECISION, FTPA_SUBMITTED ->
                assertEquals("VHH", asylumCaseResponse.read(MIGRATION_MAIN_TEXT_VISIBLE));


            default -> throw new IllegalStateException("State is not valid");
        }



    }
}
