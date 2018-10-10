package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.DirectionAppender;

@Component
public class SendBuildAppealDirectionUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final DirectionAppender directionAppender;

    public SendBuildAppealDirectionUpdater(
        @Autowired DirectionAppender directionAppender
    ) {
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.SEND_BUILD_APPEAL_DIRECTION;
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

        Direction buildAppealDirection =
            asylumCase
                .getBuildAppealDirection()
                .orElseThrow(() -> new IllegalStateException("buildAppealDirection not present"));

        Direction directionToSend =
            new Direction(
                "buildAppeal",
                buildAppealDirection
                    .getDescription()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection description not present")),
                "legalRepresentative",
                buildAppealDirection
                    .getDueDate()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection dueDate not present"))
            );

        directionAppender.append(asylumCase, directionToSend);

        asylumCase.clearBuildAppealDirection();

        return preSubmitResponse;
    }
}
