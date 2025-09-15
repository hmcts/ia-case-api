package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
@Slf4j
public class DecisionLetterDateValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String HOME_OFFICE_DECISION_LETTER_PAGE_ID = "homeOfficeDecisionLetter";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        log.info("DecisionLetterDateValidator: inside canHandle()");

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();
        String pageId = callback.getPageId();

        log.info("DecisionLetterDateValidator: canHandle() for event {} at stage {} on page ID {}", event, callbackStage, pageId);
        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && (event.equals(START_APPEAL) || event.equals(EDIT_APPEAL))
               && pageId.equals(HOME_OFFICE_DECISION_LETTER_PAGE_ID);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        log.info("DecisionLetterDateValidator: inside handle()");
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("DecisionLetterDateValidator: handling decision letter date validation");
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        String homeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Home Office decision date missing"));

        if (LocalDate.parse(homeOfficeDecisionDate).isAfter(LocalDate.now())) {
            response.addError("Home Office decision date must not be in the future.");
        }

        return response;
    }
}
