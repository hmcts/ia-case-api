package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@Component
public class RequestRespondentReviewPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final int requestRespondentReviewDueInDays;
    private final int requestRespondentReviewDueInDaysForAda;
    private final DateProvider dateProvider;
    private final DueDateService dueDateService;

    public RequestRespondentReviewPreparer(
        @Value("${requestRespondentReview.dueInDays}") int requestRespondentReviewDueInDays,
        @Value("${requestRespondentReviewAda.dueInDays}") int requestRespondentReviewDueInDaysForAda,
        DateProvider dateProvider,
        DueDateService dueDateService
    ) {
        this.requestRespondentReviewDueInDays = requestRespondentReviewDueInDays;
        this.requestRespondentReviewDueInDaysForAda = requestRespondentReviewDueInDaysForAda;
        this.dateProvider = dateProvider;
        this.dueDateService = dueDateService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        asylumCase.write(SEND_DIRECTION_EXPLANATION,
                "By the date below you must review the appellant’s ASA and bundle.\n"
                        + "The review must comply with (i) Rule 24A (3) of the Tribunal Procedure Rules 2014 and (ii) Practice Direction (1.11.2024) Part 2, section 2.1 (e), Part 3, sections 7.11 – 7.12. Specifically, the review must:\n"
                        + "- be meaningful.\n"
                        + "- explain whether you agree that the schedule of disputed issues is correct. If not, the review must set out the correct list of disputed issues, including whether there are any further issues that the respondent wishes to raise.\n"
                        + "- state whether you oppose or accept the appellant’s position on each issue and why.\n"
                        + "- cross-reference your submissions to paragraphs in the decision under appeal, pages in the respondent’s bundle, any country information evidence schedule, and/or any additional evidence relied upon.\n"
                        + "- specify which, if any, witnesses you intend to cross-examine and if you do not intend to cross-examine a witness, outline any objections to that witness’s statement being read by a judge.\n"
                        + "- address whether the appeal should be allowed on any ground if the appellant and/or their key witnesses are found to be credible according to the applicable standard of proof.\n"
                        + "- identify whether you are prepared to withdraw the decision (or part of it).\n"
                        + "- state whether the appeal can be resolved without a hearing.\n"
                        + "- not exceed 6 pages unless reasons are submitted in an accompanying application.\n"
                        + "- not contain standard or pro-forma paragraphs.\n"
                        + "- provide the name of the author of the review and the date.\n\n"
                        + "Parties must ensure they conduct proceedings with procedural rigour. The Tribunal will not overlook breaches of the requirements of the Procedure Rules, Practice Statement or Practice Direction, nor failures to comply with directions issued by the Tribunal. Parties are reminded of the sanctions for non-compliance set out in paragraph 5.3 of the Practice Direction of 01.11.24."
        );

        asylumCase.write(SEND_DIRECTION_PARTIES, Parties.RESPONDENT);
        
        LocalDate dueDate = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase)
                ? dueDateService.calculateDueDate(dateProvider.now().atStartOfDay(ZoneOffset.UTC), requestRespondentReviewDueInDaysForAda).toLocalDate()
                : dateProvider.now().plusDays(requestRespondentReviewDueInDays);
        asylumCase.write(SEND_DIRECTION_DATE_DUE, dueDate.toString());

        asylumCase.write(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
