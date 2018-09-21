package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class TimeExtensionRequestUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.REQUEST_TIME_EXTENSION;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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
