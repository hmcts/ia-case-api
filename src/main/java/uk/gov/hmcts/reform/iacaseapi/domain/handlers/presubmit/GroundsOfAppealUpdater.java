package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseArgument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.GroundOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.GroundsOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class GroundsOfAppealUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.SUBMIT_APPEAL
                   || ccdEvent.getEventId() == EventId.EDIT_GROUNDS_OF_APPEAL);
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

        List<String> appealGrounds =
            asylumCase
                .getAppealGrounds()
                .orElseThrow(() -> new IllegalStateException("appealGrounds is not present"));

        List<IdValue<GroundOfAppeal>> allGroundsOfAppeal = new ArrayList<>();

        for (int i = 0; i < appealGrounds.size(); i++) {

            String appealGround = appealGrounds.get(i);

            GroundOfAppeal groundOfAppeal = new GroundOfAppeal();

            if (appealGround.equals("refugeeConvention")) {

                groundOfAppeal.setGround(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "under the Refugee Convention"
                );
                groundOfAppeal.setExplanation(
                    asylumCase
                        .getRefugeeConventionExplanation()
                        .orElse(null)
                );
            }

            if (appealGround.equals("humanitarianProtection")) {

                groundOfAppeal.setGround(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "in relation to persons eligible for a grant of Humanitarian Protection"
                );
                groundOfAppeal.setExplanation(
                    asylumCase
                        .getHumanitarianProtectionExplanation()
                        .orElse(null)
                );
            }

            if (appealGround.equals("humanRightsConvention")) {

                groundOfAppeal.setGround(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "under the Refugee Convention"
                );
                groundOfAppeal.setExplanation(
                    asylumCase
                        .getHumanRightsConventionExplanation()
                        .orElse(null)
                );
            }

            allGroundsOfAppeal.add(
                new IdValue<>(
                    String.valueOf(i + 1),
                    groundOfAppeal
                )
            );
        }

        CaseArgument caseArgument = new CaseArgument();

        GroundsOfAppeal groundsOfAppeal =
            caseArgument
                .getGroundsOfAppeal()
                .orElse(new GroundsOfAppeal());

        groundsOfAppeal.setGroundsOfAppeal(allGroundsOfAppeal);

        caseArgument.setGroundsOfAppeal(groundsOfAppeal);

        asylumCase.setCaseArgument(caseArgument);

        return preSubmitResponse;
    }
}
