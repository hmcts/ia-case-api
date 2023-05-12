package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;

import java.time.LocalDate;
import java.util.Optional;
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
    private final int legalRepresentativeBuildCaseDueFromSubmissionDate;
    private final DateProvider dateProvider;

    public RequestCaseBuildingPreparer(
            @Value("${legalRepresentativeBuildCase.dueInDays}") int legalRepresentativeBuildCaseDueInDays,
            @Value("${legalRepresentativeBuildCase.dueInDaysFromSubmissionDate}") int legalRepresentativeBuildCaseDueFromSubmissionDate,
            DateProvider dateProvider
    ) {
        this.legalRepresentativeBuildCaseDueInDays = legalRepresentativeBuildCaseDueInDays;
        this.legalRepresentativeBuildCaseDueFromSubmissionDate = legalRepresentativeBuildCaseDueFromSubmissionDate;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        boolean validEvents = callback.getEvent() == Event.REQUEST_CASE_BUILDING
            || callback.getEvent() == Event.FORCE_REQUEST_CASE_BUILDING;
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START && validEvents;
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
                "You must now build your case to enable the respondent to conduct a thorough review of their decision.\n\n"
                        + "You have until the date indicated below to upload your Appeal Skeleton Argument and evidence.\n\n"
                        + "Your Appeal Skeleton Argument must be set out in three distinct parts to include:\n\n"
                        + "- a concise summary of the appellant’s case\n"
                        + "- a schedule of issues\n"
                        + "- why those issues should be resolved in the appellant’s favour, by reference to the evidence you have (or plan to have) and any legal authorities you rely upon\n\n"
                        + "# Next steps\n\n"
                        + "Once you've uploaded your Appeal Skeleton Argument and evidence, you should submit your case. The Tribunal Caseworker will review everything you've added.\n\n"
                        + "If your case looks ready, the Tribunal will send it to the respondent to review."
        );

        asylumCase.write(DIRECTION_PARTIES, Parties.LEGAL_REPRESENTATIVE);

        LocalDate dueDate = getBuildCaseDirectionDueDate(asylumCase, dateProvider, legalRepresentativeBuildCaseDueFromSubmissionDate, legalRepresentativeBuildCaseDueInDays);

        asylumCase.write(SEND_DIRECTION_DATE_DUE, dueDate.toString());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public static LocalDate getBuildCaseDirectionDueDate(final AsylumCase asylumCase,
                                                         final DateProvider dateProvider,
                                                         final int legalRepresentativeBuildCaseDueFromSubmissionDate,
                                                         final int legalRepresentativeBuildCaseDueInDays) {

        final Optional<String> appealSubmittedDate = asylumCase.read(APPEAL_SUBMISSION_DATE, String.class);


        // Graceful handling if appeal submission date was not found. It should not happen though. For whatever reason if submission date was not found we
        // default to legalRepresentativeBuildCaseDueInDays from current day.
        final LocalDate deadlineBySubmissionDate =
            appealSubmittedDate
                .map(localDate -> LocalDate.parse(localDate).plusDays(legalRepresentativeBuildCaseDueFromSubmissionDate))
                .orElseGet(() -> dateProvider.now().plusDays(legalRepresentativeBuildCaseDueInDays));

        final LocalDate deadlineByLetterSentOrEvidenceSubmittedDate = dateProvider.now().plusDays(legalRepresentativeBuildCaseDueInDays);

        return
            deadlineBySubmissionDate.isAfter(deadlineByLetterSentOrEvidenceSubmittedDate) ? deadlineBySubmissionDate : deadlineByLetterSentOrEvidenceSubmittedDate;
    }
}
