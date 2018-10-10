package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtensionRequest;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtensions;

@Component
public class TimeExtensionRequestUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.REQUEST_TIME_EXTENSION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        TimeExtensionRequest timeExtensionRequest =
            asylumCase
                .getTimeExtensionRequest()
                .orElseThrow(() -> new IllegalStateException("timeExtensionRequest not present"));

        List<IdValue<TimeExtension>> allTimeExtensions = new ArrayList<>();

        TimeExtensions timeExtensions =
            asylumCase
                .getTimeExtensions()
                .orElse(new TimeExtensions());

        if (timeExtensions.getTimeExtensions().isPresent()) {
            allTimeExtensions.addAll(
                timeExtensions.getTimeExtensions().get()
            );
        }

        TimeExtension newTimeExtension = new TimeExtension();

        newTimeExtension.setTimeRequested(
            timeExtensionRequest
                .getTimeRequested()
                .orElseThrow(() -> new IllegalStateException("timeRequested not present"))
        );

        newTimeExtension.setReason(
            timeExtensionRequest
                .getReason()
                .orElseThrow(() -> new IllegalStateException("reasons not present"))
        );

        newTimeExtension.setSupportingEvidence(
            timeExtensionRequest
                .getSupportingEvidence()
                .orElse(null)
        );

        newTimeExtension.setDateRequested(LocalDate.now().toString());
        newTimeExtension.setRequestedBy("Legal Rep");
        newTimeExtension.setStatus("awaitingApproval");

        allTimeExtensions.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                newTimeExtension
            )
        );

        timeExtensions.setTimeExtensions(allTimeExtensions);

        asylumCase.setTimeExtensions(timeExtensions);

        asylumCase.clearTimeExtensionRequest();

        return preSubmitResponse;
    }
}
