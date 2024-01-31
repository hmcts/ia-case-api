package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_AND_REASONS_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;

@Component
@RequiredArgsConstructor
public class DecisionAndReasonsStartedSubStateProgression implements PreSubmitCallbackHandler<AsylumCase> {

    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent().equals(Event.DECISION_AND_REASONS_STARTED);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (autoRequestHearingService.shouldAutoRequestHearing(asylumCase)) {
            asylumCase = autoRequestHearingService
                .makeAutoHearingRequest(callback, MANUAL_CREATE_HEARING_REQUIRED);
        }

        asylumCase.write(DECISION_AND_REASONS_AVAILABLE, YesOrNo.NO);
        asylumCase.write(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED, YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
