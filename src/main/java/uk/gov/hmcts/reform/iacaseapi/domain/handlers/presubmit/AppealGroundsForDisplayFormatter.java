package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_DECISION_HUMAN_RIGHTS_REFUSAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_DEPRIVATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_DEPRIVATION_HUMAN_RIGHTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_EU_REFUSAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_HUMAN_RIGHTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_HUMAN_RIGHTS_REFUSAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_PROTECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_REVOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AppealGroundsForDisplayFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPEAL
                   || callback.getEvent() == Event.EDIT_APPEAL
                   || callback.getEvent() == Event.CREATE_DLRM_CASE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        asylumCase.clear(APPEAL_GROUNDS_FOR_DISPLAY);

        Set<String> appealGrounds = new LinkedHashSet<>();

        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE);
        if (appealType.isPresent() && appealType.get().equals(AppealType.DC)) {
            Optional<CheckValues<String>> maybeAppealGroundsDeprivationHumanRights =
                asylumCase.read(APPEAL_GROUNDS_DEPRIVATION_HUMAN_RIGHTS);
            maybeAppealGroundsDeprivationHumanRights
                .filter(appealGroundsDeprivationHumanRights -> appealGroundsDeprivationHumanRights.getValues() != null)
                .ifPresent(appealGroundsDeprivationHumanRights ->
                    appealGrounds.addAll(appealGroundsDeprivationHumanRights.getValues()));

            Optional<CheckValues<String>> maybeAppealGroundsDeprivation = asylumCase.read(APPEAL_GROUNDS_DEPRIVATION);
            maybeAppealGroundsDeprivation
                .filter(appealGroundsDeprivation -> appealGroundsDeprivation.getValues() != null)
                .ifPresent(appealGroundsDeprivation ->
                    appealGrounds.addAll(appealGroundsDeprivation.getValues()));

        } else if (appealType.isPresent() && appealType.get().equals(AppealType.EA)) {
            Optional<CheckValues<String>> maybeAppealGroundsEuRefusal = asylumCase.read(APPEAL_GROUNDS_EU_REFUSAL);
            maybeAppealGroundsEuRefusal
                .filter(appealGroundsEuRefusal -> appealGroundsEuRefusal.getValues() != null)
                .ifPresent(appealGroundsEuRefusal ->
                    appealGrounds.addAll(appealGroundsEuRefusal.getValues()));

        } else if (appealType.isPresent() && appealType.get().equals(AppealType.PA)) {
            Optional<CheckValues<String>> maybeAppealGroundsProtection = asylumCase.read(APPEAL_GROUNDS_PROTECTION);
            maybeAppealGroundsProtection
                .filter(appealGroundsProtection -> appealGroundsProtection.getValues() != null)
                .ifPresent(appealGroundsProtection ->
                    appealGrounds.addAll(appealGroundsProtection.getValues()));

            Optional<CheckValues<String>> maybeAppealGroundsHumanRights = asylumCase.read(APPEAL_GROUNDS_HUMAN_RIGHTS);
            maybeAppealGroundsHumanRights
                .filter(appealGroundsHumanRights -> appealGroundsHumanRights.getValues() != null)
                .ifPresent(appealGroundsHumanRights ->
                    appealGrounds.addAll(appealGroundsHumanRights.getValues())
                );

        } else if (appealType.isPresent() && appealType.get().equals(AppealType.RP)) {
            Optional<CheckValues<String>> maybeAppealGroundsRevocation = asylumCase.read(APPEAL_GROUNDS_REVOCATION);
            maybeAppealGroundsRevocation
                .filter(appealGroundsRevocation -> appealGroundsRevocation.getValues() != null)
                .ifPresent(appealGroundsRevocation -> appealGrounds.addAll(appealGroundsRevocation.getValues()));

        } else {
            Optional<CheckValues<String>> maybeAppealGroundsHumanRightsRefusal =
                asylumCase.read(APPEAL_GROUNDS_HUMAN_RIGHTS_REFUSAL);
            maybeAppealGroundsHumanRightsRefusal
                .filter(appealGroundsHumanRightsRefusal -> appealGroundsHumanRightsRefusal.getValues() != null)
                .ifPresent(appealGroundsHumanRightsRefusal ->
                    appealGrounds.addAll(appealGroundsHumanRightsRefusal.getValues()));

            Optional<CheckValues<String>> maybeAppealGroundsDecisionHumanRightsRefusal =
                asylumCase.read(APPEAL_GROUNDS_DECISION_HUMAN_RIGHTS_REFUSAL);
            maybeAppealGroundsDecisionHumanRightsRefusal
                .filter(appealGroundsDecisionHumanRightsRefusal -> appealGroundsDecisionHumanRightsRefusal.getValues() != null)
                .ifPresent(appealGroundsDecisionHumanRightsRefusal ->
                    appealGrounds.addAll(appealGroundsDecisionHumanRightsRefusal.getValues()));
        }

        asylumCase.write(
            APPEAL_GROUNDS_FOR_DISPLAY,
            new ArrayList<>(appealGrounds)
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
