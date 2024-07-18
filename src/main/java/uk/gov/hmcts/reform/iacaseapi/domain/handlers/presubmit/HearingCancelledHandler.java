package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.HEARING_CANCELLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@Component
@RequiredArgsConstructor
public class HearingCancelledHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NextHearingDateService nextHearingDateService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(HEARING_CANCELLED);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (nextHearingDateService.enabled()) {

            asylumCase.clear(LIST_CASE_HEARING_DATE);
            NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
                .hearingId(null).hearingDateTime(null).build();
            asylumCase.write(NEXT_HEARING_DETAILS, nextHearingDetails);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
