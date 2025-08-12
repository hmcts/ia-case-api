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

        asylumCase.write(SEND_DIRECTION_EXPLANATION, getDirectionExplanation(asylumCase));

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

    private String getDirectionExplanation(AsylumCase asylumCase) {
        if (HandlerUtils.isAppellantInDetention(asylumCase) && HandlerUtils.isAppellantInPersonManual(asylumCase)) {
            return "The Home Office has uploaded their bundle of evidence.\n\n"
                    + "You must now submit an explanation of your case, also referred to as the Appellant’s Explanation of Case (AEC), and a bundle of evidence.\n\n"
                    + "The AEC should include the following:\n\n"
                    + "  1. A short summary of your case including important dates.\n"
                    + "  2. A list of things in the Home Office refusal letter that you do not agree with and why you do not agree with them.\n\n"
                    + "Your bundle of evidence should include:\n\n"
                    + "  1. An index – setting out each document and its page number.\n"
                    + "  2. A witness statement by you, the appellant.\n"
                    + "  3. A witness statement by any other person who is going to be a witness at the hearing of the appeal.\n\n"
                    + "  - Everybody who is going to be a witness at the hearing needs a witness statement.\n"
                    + "  - A witness statement is a document containing everything relevant the witness can tell the Tribunal.\n"
                    + "  - Witnesses will not be allowed to add to their statements unless the Tribunal agrees.\n"
                    + "  - Witness statements should be typed if possible.\n"
                    + "  - They must be in English.\n"
                    + "  - They must have paragraph numbers and page numbers.\n"
                    + "  - They must set out events, usually in the order they happened.\n"
                    + "  - All witness statements must be signed and dated.\n"
                    + "  - At the hearing, the Tribunal will read the witness statements. Witnesses may be asked questions about their statements by the other side and the Tribunal.\n\n"
                    + "  4. Any evidence or documents you wish to rely on in support of your appeal. This may include official documents, relationship evidence, criminal evidence, "
                    + "financial documents, medical evidence and/or country evidence. This is not an exhaustive list; your evidence will depend on the nature of your individual case.\n"
                    + "  5. If any documents are not in English, they must be translated into English by a translator. This translator must be approved by an official body. The "
                    + "translation must be certified, the translator must sign, stamp, and date it to confirm it’s a true and accurate copy of the original text.\n\n"
                    + "If you fail to submit an explanation of your case (AEC) and a bundle of evidence by the date below, you will need to request the permission of a Judge to rely on late evidence and you will have to provide an explanation for the delay.\n\n"
                    + "This decision is made by a Legal Officer in exercise of a specified power granted by the Senior President of Tribunals under rules 3(1) and (2) of the Tribunals Procedure (First-tier Tribunal) (Immigration and Asylum Chamber) Rules 2014. You may within 14 days of the date of this decision apply in writing to the Tribunal for the decision to be considered afresh by a judge under rule 3(4)";
        } else if (HandlerUtils.isAppellantInDetention(asylumCase)) {
            return "You must now build your case to enable the respondent to conduct a thorough review of the appeal.\n\n"
                    + "By the date indicated below the Appellant is directed to:\n\n"
                    + "1. Upload an Appeal Skeleton Argument (“ASA”). The form and content of this ASA must comply with the terms of Practice Direction, Part 3.\n\n"
                    + "   Specifically, the ASA must\n\n"
                    + "   - be no more than 12 pages of numbered paragraphs.\n\n"
                    + "   - be set out in three distinct parts, to include:\n"
                    + "      i) a concise summary of the appellant’s case;\n"
                    + "      ii) a schedule of the disputed issues;\n"
                    + "      iii) the appellant’s brief submissions on each of those issues, which explain why the issues should be resolved in the appellant’s favour.\n\n"
                    + "   - include the name of the author of the ASA and the date on which it was prepared.\n\n"
                    + "2. The Appellant must, by the same date, upload an indexed and paginated bundle of all evidence which must comply with the Practice Direction. This includes:\n\n"
                    + "   - Witness statements\n\n"
                    + "   - Evidence relevant to the issues set out within the ASA\n\n"
                    + "   - Expert evidence or country information evidence\n\n"
                    + "Parties must ensure they conduct proceedings with procedural rigour.\n\n"
                    + "The Tribunal will not overlook breaches of the requirements of the Procedure Rules, Practice Statement or Practice Direction, nor failures to comply with directions issued by the Tribunal."
                    + " Parties are reminded of the possible sanctions for non-compliance set out in paragraph 5.3 of the Practice Direction.\n";
        } else {
            return "You must now build your case to enable the respondent to conduct a thorough review of the appeal.\n\n"
                    + "The appellant and their representative are reminded that they have an obligation under Rule 2(4) of the Procedural Rules to help the Tribunal further the overriding objective to deal with cases fairly and justly; and to cooperate with the Tribunal generally.\n\n"
                    + "By the date indicated below the appellant is directed to:\n"
                    + "- upload an Appeal Skeleton Argument (“ASA”). The form and content of this ASA must comply with the terms of Practice Direction (1.11.2024) Part 3, sections 7.1 – 7.2; 7.6 – 7.8; and 16.2. Specifically, the ASA must\n"
                    + "   - be no more than 12 pages of numbered paragraphs.\n"
                    + "   - be set out in three distinct parts, to include:\n"
                    + "      - a concise summary of the appellant’s case;\n"
                    + "      - a schedule of the disputed issues;\n"
                    + "      - the appellant’s brief submissions on each of those issues, which explain (with reference to the decision under challenge; the evidence and appropriate legal authority) why the issues should be resolved in the appellant’s favour; and\n"
                    + "      - the name of the author of the ASA and the date on which it was prepared.\n"
                    + "- If the appellant’s application was made on or after 28 June 2022, the appellant’s brief submissions within the ASA must also expressly address the applicable provisions of the Nationality "
                    + "and Borders Act 2022 (including the different stages of the assessment set out at section 32 of NABA 2022).\n"
                    + "- The appellant must, by the same date, upload an indexed bundle of all evidence to be relied on in the appeal including witness statements and all evidence relevant to the issues in "
                    + "dispute as set out within the ASA and which has not been included in the respondent’s bundle. "
                    + "This bundle, together with any witness statements; expert evidence or country information evidence included, must comply with Practice Direction (1.11.2024) Part 3, sections 7.1, 7.2, 7.9, 8, 9 and 10.\n\n"
                    + "Parties must ensure they conduct proceedings with procedural rigour. The Tribunal will not overlook breaches of the requirements of the Procedure Rules, Practice Statement or Practice Direction, nor failures to comply with directions issued by the Tribunal. "
                    + "Parties are reminded of the sanctions for non-compliance set out in paragraph 5.3 of the Practice Direction of 01.11.24.\n";
        }
    }
}
