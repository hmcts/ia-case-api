package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_ADJOURNMENT_DETAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_HEARINGS;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdjournmentDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousHearing;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class ConvertListingLengthHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        convertNextHearingDuration(asylumCase);
        convertCurrentAdjournmentDetail(asylumCase);
        convertPreviousAdjournmentDetails(asylumCase);
        convertPreviousHearings(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void convertNextHearingDuration(AsylumCase asylumCase) {
        Optional<HoursMinutes> nextHearingDuration = asylumCase.read(NEXT_HEARING_DURATION, HoursMinutes.class);
        nextHearingDuration.ifPresent(duration -> asylumCase.write(NEXT_HEARING_DURATION, duration));
    }

    private void convertCurrentAdjournmentDetail(AsylumCase asylumCase) {
        Optional<AdjournmentDetail> currentAdjournmentDetail =
            asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class);
        currentAdjournmentDetail.ifPresent(detail -> asylumCase.write(CURRENT_ADJOURNMENT_DETAIL, detail));
    }

    private void convertPreviousAdjournmentDetails(AsylumCase asylumCase) {
        Optional<List<IdValue<AdjournmentDetail>>> previousAdjournmentDetails =
            asylumCase.read(PREVIOUS_ADJOURNMENT_DETAILS);
        previousAdjournmentDetails.ifPresent(details -> asylumCase.write(PREVIOUS_ADJOURNMENT_DETAILS, details));
    }

    private void convertPreviousHearings(AsylumCase asylumCase) {
        Optional<List<IdValue<PreviousHearing>>> previousHearings = asylumCase.read(PREVIOUS_HEARINGS);
        previousHearings.ifPresent(hearings -> asylumCase.write(PREVIOUS_HEARINGS, hearings));
    }
}
