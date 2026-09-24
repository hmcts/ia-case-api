package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTION_EDIT_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class DirectionDueDateValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public DirectionDueDateValidator(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Set<Event> eligibleEvents =  Sets.newHashSet(Event.SEND_DIRECTION,
                Event.REQUEST_CASE_EDIT,
                Event.REQUEST_RESPONDENT_EVIDENCE,
                Event.REQUEST_RESPONDENT_REVIEW,
                Event.REQUEST_CASE_BUILDING,
                Event.FORCE_REQUEST_CASE_BUILDING,
                Event.REQUEST_REASONS_FOR_APPEAL,
                Event.REQUEST_RESPONSE_AMEND,
                Event.CHANGE_DIRECTION_DUE_DATE);

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && eligibleEvents.contains(callback.getEvent())
                && (callback.getPageId().equals("sendDirection")
                    || callback.getPageId().equals("requestRespondentEvidence")
                    || callback.getPageId().equals("requestCaseEdit")
                    || callback.getPageId().equals("requestRespondentReview")
                    || callback.getPageId().equals("requestCaseBuilding")
                    || callback.getPageId().equals("changeDirectionDueDate"));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response =
                new PreSubmitCallbackResponse<>(asylumCase);

        if (callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE) {
            Optional<String> directionEditDueDate =
                    asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class);
            log.info("Direction edit due date: {}", directionEditDueDate.orElse("not present"));
            validateDueDate(directionEditDueDate, response, callback);
        } else {
            Optional<String> sendDirectionDueDate =
                    asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class);

            validateDueDate(sendDirectionDueDate, response, callback);
        }

        return response;
    }

    private void validateDueDate(
            Optional<String> dueDate,
            PreSubmitCallbackResponse<AsylumCase> response,
            Callback<AsylumCase> callback
    ) {
        if (dueDate.isPresent()) {
            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            LocalDate parsedDate = LocalDate.parse(dueDate.get());
            Optional<String> directionEditDueDate =
                    asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class);
            log.info("Direction edit due date, validator: {}", directionEditDueDate.orElse("not present"));
            if (parsedDate.isBefore(dateProvider.now())) {
                response.addError(
                        "The date entered is not valid - this must be today or a date in the future"
                );
            }
        }
    }
}
