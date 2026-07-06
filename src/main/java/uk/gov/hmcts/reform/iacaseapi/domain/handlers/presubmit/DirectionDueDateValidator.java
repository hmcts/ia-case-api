package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION_WITH_QUESTIONS;

import java.time.LocalDate;
import java.util.Optional;

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

        Event event = callback.getEvent();
        log.info("Stage={}, Event={}, PageId={}",
                callbackStage,
                callback.getEvent(),
                callback.getPageId());

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (event.equals(SEND_DIRECTION) || event.equals(SEND_DIRECTION_WITH_QUESTIONS));
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

        Optional<String> dueDate =
                asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class);

        log.info("SEND_DIRECTION_DATE_DUE = {}", dueDate);

        if (dueDate.isPresent()) {
            LocalDate parsedDate = LocalDate.parse(dueDate.get());

            if (parsedDate.isBefore(dateProvider.now())) {
                response.addError("Direction due date must be today or in the future.");
            }
        }

        return response;
    }
}
