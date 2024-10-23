package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MIGRATION_HMC_SECOND_PART_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MIGRATION_MAIN_TEXT_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.AWAITING_RESPONDENT_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.CASE_UNDER_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.ENDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.FTPA_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.FTPA_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.REASONS_FOR_APPEAL_SUBMITTED;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class MigrateAriaCasesDocumentUploaderMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.PROGRESS_MIGRATED_CASE
            && callback.getPageId().equals("migrateAriaCasesDocumentUploaderMidEvent");
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final State ariaDesiredState = asylumCase.read(ARIA_DESIRED_STATE, State.class)
            .orElseThrow(() -> new IllegalStateException("ariaDesiredState is not present"));

        final Optional<UpdateTribunalRules> updateTribunalDecisionRules = asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class);
        hideMigrationText(asylumCase);
        Optional<List<IdValue<DocumentWithDescription>>> maybeRespondentEvidence = asylumCase.read(RESPONDENT_EVIDENCE);


        if (ariaDesiredState.equals(APPEAL_SUBMITTED) || (ariaDesiredState.equals(AWAITING_RESPONDENT_EVIDENCE) && maybeRespondentEvidence.isPresent())
            || ariaDesiredState.equals(CASE_UNDER_REVIEW) || ariaDesiredState.equals(REASONS_FOR_APPEAL_SUBMITTED) || ariaDesiredState.equals(LISTING) ||
            ariaDesiredState.equals(DECISION) || ariaDesiredState.equals(FTPA_SUBMITTED)) {
            asylumCase.write(MIGRATION_MAIN_TEXT_VISIBLE, "VHH");
        } else if (ariaDesiredState.equals(PREPARE_FOR_HEARING) || (ariaDesiredState.equals(DECIDED) && updateTribunalDecisionRules.isPresent())) {
            asylumCase.write(MIGRATION_MAIN_TEXT_VISIBLE, "HMC");
            asylumCase.write(MIGRATION_HMC_SECOND_PART_VISIBLE, "Yes");

        } else if (ariaDesiredState.equals(AWAITING_RESPONDENT_EVIDENCE)) {
            asylumCase.write(MIGRATION_MAIN_TEXT_VISIBLE, "MoveIT");

        } else if (ariaDesiredState.equals(DECIDED) || ariaDesiredState.equals(FTPA_DECIDED) || ariaDesiredState.equals(ENDED)) {
            asylumCase.write(MIGRATION_MAIN_TEXT_VISIBLE, "VHHToCCD");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static void hideMigrationText(AsylumCase asylumCase) {
        asylumCase.write(MIGRATION_MAIN_TEXT_VISIBLE, "");
        asylumCase.write(MIGRATION_HMC_SECOND_PART_VISIBLE, "");
    }

}