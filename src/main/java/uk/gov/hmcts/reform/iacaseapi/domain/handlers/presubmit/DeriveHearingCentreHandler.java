package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

@Component
public class DeriveHearingCentreHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;

    public DeriveHearingCentreHandler(
        HearingCentreFinder hearingCentreFinder
    ) {
        this.hearingCentreFinder = hearingCentreFinder;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL;
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

        if (!asylumCase.getHearingCentre().isPresent()) {

            trySetHearingCentreFromPostcode(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<String> getAppellantPostcode(
        AsylumCase asylumCase
    ) {
        if (asylumCase.getAppellantHasFixedAddress().orElse(YesOrNo.NO) == YesOrNo.YES) {

            Optional<AddressUk> optionalAppellantAddress = asylumCase.getAppellantAddress();

            if (optionalAppellantAddress.isPresent()) {

                AddressUk appellantAddress = optionalAppellantAddress.get();

                return appellantAddress.getPostCode();
            }
        }

        return Optional.empty();
    }

    private void trySetHearingCentreFromPostcode(
        AsylumCase asylumCase
    ) {
        Optional<String> optionalAppellantPostcode = getAppellantPostcode(asylumCase);

        if (optionalAppellantPostcode.isPresent()) {

            String appellantPostcode = optionalAppellantPostcode.get();
            asylumCase.setHearingCentre(
                hearingCentreFinder.find(appellantPostcode)
            );

        } else {
            asylumCase.setHearingCentre(hearingCentreFinder.getDefaultHearingCentre());
        }
    }
}
