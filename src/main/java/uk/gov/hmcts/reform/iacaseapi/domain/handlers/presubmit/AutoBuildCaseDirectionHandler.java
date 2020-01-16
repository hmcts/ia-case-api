package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RequestCaseBuildingPreparer.getBuildCaseDirectionDueDate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class AutoBuildCaseDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int buildCaseDueInDays;
    private final int legalRepresentativeBuildCaseDueFromSubmissionDate;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public AutoBuildCaseDirectionHandler(
        @Value("${legalRepresentativeBuildCase.dueInDays}") int buildCaseDueInDays,
        @Value("${legalRepresentativeBuildCase.dueInDaysFromSubmissionDate}") int legalRepresentativeBuildCaseDueFromSubmissionDate,
        DateProvider dateProvider,
        DirectionAppender directionAppender
    ) {
        this.buildCaseDueInDays = buildCaseDueInDays;
        this.legalRepresentativeBuildCaseDueFromSubmissionDate = legalRepresentativeBuildCaseDueFromSubmissionDate;
        this.dateProvider = dateProvider;
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE;
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

        Optional<List<IdValue<Direction>>> maybeDirections =
                asylumCase.read(DIRECTIONS);

        final List<IdValue<Direction>> existingDirections =
            maybeDirections.orElse(Collections.emptyList());

        boolean directionAlreadyExists =
            existingDirections
                .stream()
                .map(IdValue::getValue)
                .anyMatch(direction -> direction.getTag() == DirectionTag.BUILD_CASE);

        if (!directionAlreadyExists) {

            List<IdValue<Direction>> allDirections =
                directionAppender.append(
                    existingDirections,
                    "You must now build your case by uploading your Appeal Skeleton Argument and evidence. You have 42 days from the date you submitted the appeal, or 28 days from the date of this email, whichever occurs later.\n\n"
                    + "You must write a full skeleton argument that references:\n\n"
                    + "- all the evidence you have (or plan) to rely on, including witness statements\n"
                    + "- the grounds and issues of the case\n"
                    + "- any new matters\n"
                    + "- any legal authorities you plan to rely on and why they are applicable to your case\n\n"
                    + "Your argument must explain why you believe the respondent's decision is wrong. You must provide all the information for the respondent to conduct a thorough review of their decision.\n\n"
                    + "# Next steps\n\n"
                    + "Once you have uploaded your appeal argument and evidence, you should submit your case. The Tribunal case worker will review everything you've added.\n\n"
                    + "If your case looks ready, the Tribunal case worker will send it to the respondent to review.",
                    Parties.LEGAL_REPRESENTATIVE,
                    getBuildCaseDirectionDueDate(asylumCase, dateProvider, legalRepresentativeBuildCaseDueFromSubmissionDate, buildCaseDueInDays).toString(),
                    DirectionTag.BUILD_CASE
                );

            asylumCase.write(DIRECTIONS, allDirections);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
