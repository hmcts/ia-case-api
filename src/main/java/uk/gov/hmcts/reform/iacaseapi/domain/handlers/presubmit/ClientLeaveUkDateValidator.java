package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CLIENT_LEAVE_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ClientLeaveUkDateValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String CLIENT_DEPARTURE_DATE_PAGE_ID = "departureDate";

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
               && pageId.equals(CLIENT_DEPARTURE_DATE_PAGE_ID);
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

        String clientDepartureFromUkDate = asylumCase.read(DATE_CLIENT_LEAVE_UK, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Client departure from UK date missing"));

        if (LocalDate.parse(clientDepartureFromUkDate).isAfter(LocalDate.now())) {
            response.addError("Client departure from UK date must not be in the future.");
        }
    
        return response;
    }
}
