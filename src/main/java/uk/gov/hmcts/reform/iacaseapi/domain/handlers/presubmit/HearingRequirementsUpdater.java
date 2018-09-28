package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingRequirements;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterRequirement;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class HearingRequirementsUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.SUBMIT_HEARING_REQUIREMENTS;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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
