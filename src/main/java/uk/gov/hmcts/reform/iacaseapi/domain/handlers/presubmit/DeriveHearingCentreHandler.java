package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

@Component
public class DeriveHearingCentreHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final HearingCentreFinder hearingCentreFinder;

    public DeriveHearingCentreHandler(
        HearingCentreFinder hearingCentreFinder
    ) {
        this.hearingCentreFinder = hearingCentreFinder;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        if (!CaseDataMap.getHearingCentre().isPresent()) {

            trySetHearingCentreFromPostcode(CaseDataMap);
        }

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }

    private Optional<String> getAppellantPostcode(
        CaseDataMap CaseDataMap
    ) {
        if (CaseDataMap.getAppellantHasFixedAddress().orElse(YesOrNo.NO) == YesOrNo.YES) {

            Optional<AddressUk> optionalAppellantAddress = CaseDataMap.getAppellantAddress();

            if (optionalAppellantAddress.isPresent()) {

                AddressUk appellantAddress = optionalAppellantAddress.get();

                return appellantAddress.getPostCode();
            }
        }

        return Optional.empty();
    }

    private void trySetHearingCentreFromPostcode(
        CaseDataMap CaseDataMap
    ) {
        Optional<String> optionalAppellantPostcode = getAppellantPostcode(CaseDataMap);

        if (optionalAppellantPostcode.isPresent()) {

            String appellantPostcode = optionalAppellantPostcode.get();
            CaseDataMap.setHearingCentre(
                hearingCentreFinder.find(appellantPostcode)
            );

        } else {
            CaseDataMap.setHearingCentre(hearingCentreFinder.getDefaultHearingCentre());
        }
    }
}
