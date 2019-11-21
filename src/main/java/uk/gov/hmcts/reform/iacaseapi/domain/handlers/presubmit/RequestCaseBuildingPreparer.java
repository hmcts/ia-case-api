package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RequestCaseBuildingPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final int legalRepresentativeBuildCaseDueInDays;
    private final DateProvider dateProvider;

    public RequestCaseBuildingPreparer(
            @Value("${legalRepresentativeBuildCase.dueInDays}") int legalRepresentativeBuildCaseDueInDays,
            DateProvider dateProvider
    ) {
        this.legalRepresentativeBuildCaseDueInDays = legalRepresentativeBuildCaseDueInDays;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.REQUEST_CASE_BUILDING;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        asylumCase.write(SEND_DIRECTION_EXPLANATION,
                "You must now build your case by uploading your Appeal Skeleton Argument and evidence. You have 28 days from the date of this email to complete this.\n\n"
                        + "You must write a full skeleton argument that references:\n\n"
                        + "- all the evidence you have (or plan) to rely on, including any witness statements\n"
                        + "- the grounds and issues of the case\n"
                        + "- any new matters\n"
                        + "- any legal authorities you plan to rely on and why they are applicable to your case\n\n"
                        + "Your argument must explain why you believe the respondent’s decision is wrong. You must provide all the information for the respondent to conduct a thorough review of their decision.\n\n"
                        + "# Next steps\n\n"
                        + "Once you've uploaded your appeal argument and evidence, you should submit your case. The Tribunal case worker will review everything you've added.\n\n"
                        + "If your case looks ready, the Tribunal case worker will send it to the respondent to review."
        );

        asylumCase.write(SEND_DIRECTION_PARTIES, Parties.LEGAL_REPRESENTATIVE);

        asylumCase.write(SEND_DIRECTION_DATE_DUE,
                dateProvider
                        .now()
                        .plusDays(legalRepresentativeBuildCaseDueInDays)
                        .toString()
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
