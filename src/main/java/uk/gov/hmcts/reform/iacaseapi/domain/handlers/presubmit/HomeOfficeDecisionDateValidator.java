package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Component
public class HomeOfficeDecisionDateValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String HOME_OFFICE_DECISION_LETTER_PAGE_ID = "homeOfficeDecisionLetter";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();
        String pageId = callback.getPageId();

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && (event.equals(START_APPEAL) || event.equals(EDIT_APPEAL))
               && pageId.equals(HOME_OFFICE_DECISION_LETTER_PAGE_ID);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Appeal type is missing"));

        if (asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).orElse(NO).equals(YES)) {
            if (appealType.equals(AppealType.AG)) {
                String dateOfDecisionLetter = asylumCase.read(DATE_ON_DECISION_LETTER, String.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Date of decision letter missing"));

                if (LocalDate.parse(dateOfDecisionLetter).isAfter(LocalDate.now())) {
                    response.addError("Date of decision letter must not be in the future.");
                }
            } else {
                String homeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Home Office decision date missing"));

                if (LocalDate.parse(homeOfficeDecisionDate).isAfter(LocalDate.now())) {
                    response.addError("Home Office decision date must not be in the future.");
                }
            }
        } else { // out-of-country appeal
            String decisionLetterReceivedDate = asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class)
                    .orElseThrow(() -> new RequiredFieldMissingException("Decision letter received date missing"));

            if (LocalDate.parse(decisionLetterReceivedDate).isAfter(LocalDate.now())) {
                response.addError("Decision letter received date must not be in the future.");
            }
        }

        return response;
    }
}
