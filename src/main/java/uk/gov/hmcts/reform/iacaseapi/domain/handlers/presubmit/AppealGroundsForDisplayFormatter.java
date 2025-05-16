package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_DEPRIVATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_EU_REFUSAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_FOR_DISPLAY;
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
                   || callback.getEvent() == Event.EDIT_APPEAL);
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
        } else if (appealType.isPresent() && appealType.get().equals(AppealType.RP)) {
            Optional<CheckValues<String>> maybeAppealGroundsRevocation = asylumCase.read(APPEAL_GROUNDS_REVOCATION);
            maybeAppealGroundsRevocation
                .filter(appealGroundsRevocation -> appealGroundsRevocation.getValues() != null)
                .ifPresent(appealGroundsRevocation -> appealGrounds.addAll(appealGroundsRevocation.getValues()));
        }

        asylumCase.write(
            APPEAL_GROUNDS_FOR_DISPLAY,
            new ArrayList<>(appealGrounds)
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
