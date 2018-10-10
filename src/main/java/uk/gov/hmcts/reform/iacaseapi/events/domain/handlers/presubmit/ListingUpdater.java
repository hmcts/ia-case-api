package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Listing;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ListingUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.RECORD_LISTING;
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

        String listingHearingCentre =
            asylumCase
                .getListingHearingCentre()
                .orElseThrow(() -> new IllegalStateException("listingHearingCentre is not present"));

        String listingHearingLength =
            asylumCase
                .getListingHearingLength()
                .orElseThrow(() -> new IllegalStateException("listingHearingLength is not present"));

        String listingHearingDate =
            asylumCase
                .getListingHearingDate()
                .orElseThrow(() -> new IllegalStateException("listingHearingDate is not present"));

        asylumCase.setListing(new Listing(
            listingHearingCentre,
            listingHearingLength,
            listingHearingDate
        ));

        return preSubmitResponse;
    }
}
