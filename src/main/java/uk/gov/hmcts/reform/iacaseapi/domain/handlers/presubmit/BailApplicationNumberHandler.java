package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.BailApplicationStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Service
public class BailApplicationNumberHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final Set<String> DETENTION_FACILITY_PAGE_IDS = Set.of(
          "hasPendingBailApplications", "markAppealAsDetained_appellantBailApplication");

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && (callback.getEvent() == Event.START_APPEAL
               || callback.getEvent() == Event.EDIT_APPEAL
               || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
               || callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED)
               && DETENTION_FACILITY_PAGE_IDS.contains(callback.getPageId());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        String bailReferenceNumber = asylumCase.read(BAIL_APPLICATION_NUMBER, String.class).orElse("");

        Optional<BailApplicationStatus> status = asylumCase.read(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.class);

        if (status.isPresent() && status.get() == BailApplicationStatus.YES) {
            if (!bailReferenceNumber.matches("([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})") &&  !bailReferenceNumber.matches("[a-zA-Z]{2}\\/[0-9]{5}")) {
                response.addError("Invalid bail number provided. The bail number must be either 16 digits with dashes "
                                  + "(e.g. 1111-2222-3333-4444) or 8 characters long (e.g. HW/12345)");
            }
        }

        return response;
    }

}
