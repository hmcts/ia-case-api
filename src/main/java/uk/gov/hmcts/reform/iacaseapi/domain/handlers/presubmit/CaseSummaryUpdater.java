package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class CaseSummaryUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.SUBMIT_APPEAL;
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

        List<IdValue<GroundForAppeal>> allGroundsForAppeal = new ArrayList<>();

        for (int i = 0; i < appealGrounds.size(); i++) {

            String appealGround = appealGrounds.get(i);

            GroundForAppeal groundForAppeal = new GroundForAppeal();

            if (appealGround.equals("refugeeConvention")) {

                groundForAppeal.setGround(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "under the Refugee Convention"
                );
                groundForAppeal.setExplanation(
                    asylumCase
                        .getRefugeeConventionExplanation()
                        .orElse(null)
                );
            }

            if (appealGround.equals("humanitarianProtection")) {

                groundForAppeal.setGround(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "in relation to persons eligible for a grant of Humanitarian Protection"
                );
                groundForAppeal.setExplanation(
                    asylumCase
                        .getHumanitarianProtectionExplanation()
                        .orElse(null)
                );
            }

            if (appealGround.equals("humanRightsConvention")) {

                groundForAppeal.setGround(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "under the Refugee Convention"
                );
                groundForAppeal.setExplanation(
                    asylumCase
                        .getHumanRightsConventionExplanation()
                        .orElse(null)
                );
            }

            allGroundsForAppeal.add(
                new IdValue<>(
                    String.valueOf(i + 1),
                    groundForAppeal
                )
            );
        }

        CaseSummary caseSummary = new CaseSummary();

        GroundsForAppeal groundsForAppeal =
            caseSummary
                .getGroundsForAppeal()
                .orElse(new GroundsForAppeal());

        groundsForAppeal.setGroundsForAppeal(allGroundsForAppeal);

        caseSummary.setGroundsForAppeal(groundsForAppeal);

        asylumCase.setCaseSummary(caseSummary);

        return preSubmitResponse;
    }
}
