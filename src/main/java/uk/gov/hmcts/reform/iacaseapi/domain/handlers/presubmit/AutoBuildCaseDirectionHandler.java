package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.DIRECTIONS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
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
public class AutoBuildCaseDirectionHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final int buildCaseDueInDays;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public AutoBuildCaseDirectionHandler(
        @Value("${legalRepresentativeBuildCase.dueInDays}") int buildCaseDueInDays,
        DateProvider dateProvider,
        DirectionAppender directionAppender
    ) {
        this.buildCaseDueInDays = buildCaseDueInDays;
        this.dateProvider = dateProvider;
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<Direction>>> maybeDirections =
                caseDataMap.get(DIRECTIONS);

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
                    "You must now build your case by uploading your appeal argument and evidence.\n\n"
                    + "Advice on writing an appeal argument\n"
                    + "You must write a full argument that references:\n"
                    + "- all the evidence you have or plan to rely on, including any witness statements\n"
                    + "- the grounds and issues of the case\n"
                    + "- any new matters\n"
                    + "- any legal authorities you plan to rely on and why they are applicable to your case\n\n"
                    + "Your argument must explain why you believe the respondent's decision is wrong. You must provide all "
                    + "the information for the Home Office to conduct a thorough review of their decision at this stage.\n\n"
                    + "Next steps\n"
                    + "Once you have uploaded your appeal argument and all evidence, submit your case. The case officer will "
                    + "then review everything you've added. If your case looks ready, the case officer will send it to the "
                    + "respondent for their review. The respondent then has 14 days to respond.",
                    Parties.LEGAL_REPRESENTATIVE,
                    dateProvider
                        .now()
                        .plusDays(buildCaseDueInDays)
                        .toString(),
                    DirectionTag.BUILD_CASE
                );

            caseDataMap.write(DIRECTIONS, allDirections);
        }

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
