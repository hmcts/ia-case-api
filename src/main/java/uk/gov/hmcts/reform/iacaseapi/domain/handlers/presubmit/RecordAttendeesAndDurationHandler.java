package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Component
@RequiredArgsConstructor
public class RecordAttendeesAndDurationHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final IaHearingsApiService iaHearingsApiService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_ATTENDEES_AND_DURATION;
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

        iaHearingsApiService.aboutToSubmit(callback);

        // the participants have been sent to HMC, so they are not kept on the case; a saved list would make
        // the next run of the event look like it deletes rows, which needs a delete permission users don't have
        asylumCase.clear(ATTENDEES_AND_DURATION_PARTICIPANTS);
        asylumCase.clear(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE);

        asylumCase.write(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
