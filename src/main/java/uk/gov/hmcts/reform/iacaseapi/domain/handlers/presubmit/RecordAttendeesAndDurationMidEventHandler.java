package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class RecordAttendeesAndDurationMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final CommonRefDataDynamicListProvider provider;
    private final LocationRefDataService locationRefDataService;
    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.RECORD_ATTENDEES_AND_DURATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
