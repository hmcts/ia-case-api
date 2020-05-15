package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RequestCmaRequirementsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public static final int DUE_IN_WEEKS = 1;
    private final DateProvider dateProvider;

    @Autowired
    public RequestCmaRequirementsPreparer(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.REQUEST_CMA_REQUIREMENTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String dueDate = dateProvider.now().plusWeeks(DUE_IN_WEEKS).toString();

        asylumCase.write(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, dueDate);
        asylumCase.write(AsylumCaseFieldDefinition.REQUEST_CMA_REQUIREMENTS_REASONS, "");

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
