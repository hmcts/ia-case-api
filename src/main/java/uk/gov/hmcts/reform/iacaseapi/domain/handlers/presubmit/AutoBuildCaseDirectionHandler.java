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
                        asylumCase,
                    existingDirections,
                    """
                    You must now build your case to enable the respondent to conduct a thorough review of their decision.
                    
                    You have until the date indicated below to upload your Appeal Skeleton Argument and evidence.
                    
                    Your Appeal Skeleton Argument must be set out in three distinct parts to include:
                    
                    - a concise summary of the appellant’s case
                    - a schedule of issues
                    - why those issues should be resolved in the appellant’s favour, by reference to the evidence you have (or plan to have) and any legal authorities you rely upon
                    
                    # Next steps
                    
                    Once you've uploaded your Appeal Skeleton Argument and evidence, you should submit your case. The Legal Officer will review everything you've added.
                    
                    If your case looks ready, the Tribunal will send it to the respondent to review.""",
                    Parties.LEGAL_REPRESENTATIVE,
                    getBuildCaseDirectionDueDate(asylumCase, dateProvider, legalRepresentativeBuildCaseDueFromSubmissionDate, buildCaseDueInDays).toString(),
                    DirectionTag.BUILD_CASE
                );

            asylumCase.write(DIRECTIONS, allDirections);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
