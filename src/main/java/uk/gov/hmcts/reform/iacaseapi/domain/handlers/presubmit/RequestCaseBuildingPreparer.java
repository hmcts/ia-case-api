package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegallyRepresentedEjpCase;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@Component
public class RequestCaseBuildingPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final int legalRepresentativeBuildCaseDueInDays;
    private final int legalRepresentativeBuildCaseDueFromSubmissionDate;
    private final int legalRepresentativeBuildCaseDueInDaysAda;
    private final DateProvider dateProvider;
    private final DueDateService dueDateService;

    public RequestCaseBuildingPreparer(
            @Value("${legalRepresentativeBuildCase.dueInDays}") int legalRepresentativeBuildCaseDueInDays,
            @Value("${legalRepresentativeBuildCase.dueInDaysFromSubmissionDate}") int legalRepresentativeBuildCaseDueFromSubmissionDate,
            @Value("${legalRepresentativeBuildCaseAda.dueInDay}") int legalRepresentativeBuildCaseDueInDaysAda,
            DueDateService dueDateService,
            DateProvider dateProvider
    ) {
        this.legalRepresentativeBuildCaseDueInDays = legalRepresentativeBuildCaseDueInDays;
        this.legalRepresentativeBuildCaseDueFromSubmissionDate = legalRepresentativeBuildCaseDueFromSubmissionDate;
        this.legalRepresentativeBuildCaseDueInDaysAda = legalRepresentativeBuildCaseDueInDaysAda;
        this.dueDateService = dueDateService;
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
                "You must now build your case to enable the respondent to conduct a thorough review of the appeal.\n\n"
                        + "The appellant and their representative are reminded that they have an obligation under Rule 2(4) of the Procedural Rules to help the Tribunal further the overriding objective to deal with cases fairly and justly; and to cooperate with the Tribunal generally.\n\n"
                        + "By the date indicated below the appellant is directed to:\n"
                        + "-upload an Appeal Skeleton Argument (“ASA”). The form and content of this ASA must comply with the terms of Practice Direction (1.11.2024) Part 3, sections 7.1 – 7.2; 7.6 – 7.8; and 16.2. Specifically, the ASA must\n"
                        + "   -be no more than 12 pages of numbered paragraphs.\n"
                        + "   -be set out in three distinct parts, to include:\n"
                        + "      -a concise summary of the appellant’s case;\n"
                        + "      -a schedule of the disputed issues;\n"
                        + "      -the appellant’s brief submissions on each of those issues, which explain (with reference to the decision under challenge; the evidence and appropriate legal authority) why the issues should be resolved in the appellant’s favour; and\n"
                        + "      -the name of the author of the ASA and the date on which it was prepared.\n"
                        + "-If the appellant’s application was made on or after 28 June 2022, the appellant’s brief submissions within the ASA must also expressly address the applicable provisions of the Nationality "
                        + "and Borders Act 2022 (including the different stages of the assessment set out at section 32 of NABA 2022).\n"
                        + "-The appellant must, by the same date, upload an indexed bundle of all evidence to be relied on in the appeal including witness statements and all evidence relevant to the issues in "
                        + "dispute as set out within the ASA and which has not been included in the respondent’s bundle. "
                        + "This bundle, together with any witness statements; expert evidence or country information evidence included, must comply with Practice Direction (1.11.2024) Part 3, sections 7.1, 7.2, 7.9, 8, 9 and 10.\n"
                        + "Parties must ensure they conduct proceedings with procedural rigour. The Tribunal will not overlook breaches of the requirements of the Procedure Rules, Practice Statement or Practice Direction, nor failures to comply with directions issued by the Tribunal. "
                        + "Parties are reminded of the sanctions for non-compliance set out in paragraph 5.3 of the Practice Direction of 01.11.24.\n"
        );

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);
        boolean isInternalDetained = (isInternalCase(asylumCase) && isAppellantInDetention(asylumCase));
        boolean isEjpUnrepNonDetained = (isEjpCase(asylumCase) && !isAppellantInDetention(asylumCase) && !isLegallyRepresentedEjpCase(asylumCase));

        if (isAcceleratedDetainedAppeal) {
            ZonedDateTime dueDateTime = dueDateService.calculateDueDate(ZonedDateTime.now(), legalRepresentativeBuildCaseDueInDaysAda);
            LocalDate dueDate = dueDateTime.toLocalDate();
            asylumCase.write(SEND_DIRECTION_DATE_DUE, dueDate.toString());
        } else {
            LocalDate dueDate = getBuildCaseDirectionDueDate(asylumCase, dateProvider, legalRepresentativeBuildCaseDueFromSubmissionDate, legalRepresentativeBuildCaseDueInDays);
            asylumCase.write(SEND_DIRECTION_DATE_DUE, dueDate.toString());
        }

        if (isInternalDetained || isEjpUnrepNonDetained) {
            asylumCase.write(SEND_DIRECTION_PARTIES, Parties.APPELLANT);
        } else {
            asylumCase.write(SEND_DIRECTION_PARTIES, Parties.LEGAL_REPRESENTATIVE);
        }

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
