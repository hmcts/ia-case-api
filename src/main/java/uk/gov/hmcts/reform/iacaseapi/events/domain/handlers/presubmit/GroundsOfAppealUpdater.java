package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.CaseArgument;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.GroundOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.GroundsOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

@Component
public class GroundsOfAppealUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEventId() == EventId.SUBMIT_APPEAL
                   || callback.getEventId() == EventId.EDIT_GROUNDS_OF_APPEAL);
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

        List<String> appealGrounds =
            asylumCase
                .getAppealGrounds()
                .orElseThrow(() -> new IllegalStateException("appealGrounds is not present"));

        List<IdValue<GroundOfAppeal>> allGroundsOfAppeal = new ArrayList<>();

        for (int i = 0; i < appealGrounds.size(); i++) {

            String appealGround = appealGrounds.get(i);

            GroundOfAppeal groundOfAppeal = null;

            if (appealGround.equals("refugeeConvention")) {

                groundOfAppeal = new GroundOfAppeal(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "under the Refugee Convention"
                );
            }

            if (appealGround.equals("humanitarianProtection")) {

                groundOfAppeal = new GroundOfAppeal(
                    "Removing the appellant from the UK would breach the UK's obligations "
                    + "in relation to persons eligible for a grant of humanitarian protection"
                );
            }

            if (appealGround.equals("humanRightsConvention")) {

                groundOfAppeal = new GroundOfAppeal(
                    "Removing the appellant from the UK would be unlawful under section 6 of the "
                    + "Human Rights Act 1998 (public authority not to act contrary to Human Rights Convention)"
                );
            }

            if (groundOfAppeal != null) {

                allGroundsOfAppeal.add(
                    new IdValue<>(
                        appealGround,
                        groundOfAppeal
                    )
                );
            }
        }

        CaseArgument caseArgument =
            asylumCase
                .getCaseArgument()
                .orElse(new CaseArgument());

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
