package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class StartAppealMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final Pattern HOME_OFFICE_REF_PATTERN = Pattern.compile("^(([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})|([0-9]{1,9}))$");

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.START_APPEAL;
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

        String homeOfficeReferenceNumber = asylumCase
            .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        if (!HOME_OFFICE_REF_PATTERN.matcher(homeOfficeReferenceNumber).matches()) {
            response.addError("Home Office CID field is wrong, please correct");
        }

        return response;
    }
}
