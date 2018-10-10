package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.HearingRequirements;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.InterpreterRequirement;

@Component
public class HearingRequirementsUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.SUBMIT_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        String hearingRequirementsAppellantAttending =
            asylumCase
                .getHearingRequirementsAppellantAttending()
                .orElseThrow(() -> new IllegalStateException("hearingRequirementsAppellantAttending not present"));

        String hearingRequirementsAppellantGivingOralEvidence =
            asylumCase
                .getHearingRequirementsAppellantGivingOralEvidence()
                .orElse(null);

        String hearingRequirementsAnyWitnesses =
            asylumCase
                .getHearingRequirementsAnyWitnesses()
                .orElseThrow(() -> new IllegalStateException("hearingRequirementsAnyWitnesses not present"));

        List<IdValue<String>> hearingRequirementsWitnesses =
            asylumCase
                .getHearingRequirementsWitnesses()
                .orElse(Collections.emptyList());

        String hearingRequirementsInterpreterRequired =
            asylumCase
                .getHearingRequirementsInterpreterRequired()
                .orElseThrow(() -> new IllegalStateException("hearingRequirementsInterpreterRequired not present"));

        List<IdValue<InterpreterRequirement>> hearingRequirementsInterpreters =
            asylumCase
                .getHearingRequirementsInterpreters()
                .orElse(Collections.emptyList());

        String hearingRequirementsAdjustmentsApply =
            asylumCase
                .getHearingRequirementsAdjustmentsApply()
                .orElseThrow(() -> new IllegalStateException("hearingRequirementsAdjustmentsApply not present"));

        List<String> hearingRequirementsAdjustments =
            asylumCase
                .getHearingRequirementsAdjustments()
                .orElse(Collections.emptyList());

        String hearingRequirementsAdjustmentsOther =
            asylumCase
                .getHearingRequirementsAdjustmentsOther()
                .orElse(null);

        asylumCase
            .setHearingRequirements(
                new HearingRequirements(
                    hearingRequirementsAppellantAttending,
                    hearingRequirementsAppellantGivingOralEvidence,
                    hearingRequirementsAnyWitnesses,
                    hearingRequirementsWitnesses,
                    hearingRequirementsInterpreterRequired,
                    hearingRequirementsInterpreters,
                    hearingRequirementsAdjustmentsApply,
                    hearingRequirementsAdjustments,
                    hearingRequirementsAdjustmentsOther
                )
            );

        return preSubmitResponse;
    }
}
