package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_CMA_REQUIREMENTS_REASONS;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class RequestCmaRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    @Autowired
    public RequestCmaRequirementsHandler(DateProvider dateProvider, DirectionAppender directionAppender) {
        this.dateProvider = dateProvider;
        this.directionAppender = directionAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_CMA_REQUIREMENTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String directionDueDate = asylumCase.<String>read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE).orElse("");
        if (!LocalDate.parse(directionDueDate).isAfter(dateProvider.now())) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

            response.addError("Direction due date must be in the future");
            return response;
        }

        String requirementsReasons = asylumCase.<String>read(REQUEST_CMA_REQUIREMENTS_REASONS).orElse("");
        String explanation = "You need to attend a case management appointment. This is a meeting with a Tribunal "
                + "Caseworker to talk about your appeal. A Home Office representative may also be at the meeting.\n"
                + "\n"
                + requirementsReasons;

        List<IdValue<Direction>> allDirections = directionAppender.append(
                asylumCase,
                asylumCase.<List<IdValue<Direction>>>read(DIRECTIONS).orElse(Collections.emptyList()),
                explanation,
                Parties.APPELLANT,
                directionDueDate,
                DirectionTag.REQUEST_CMA_REQUIREMENTS
        );
        asylumCase.write(DIRECTIONS, allDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
