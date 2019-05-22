package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

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

        final CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        if (!caseDataMap.get(HEARING_CENTRE).isPresent()) {

            trySetHearingCentreFromPostcode(caseDataMap);
        }

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }

    private Optional<String> getAppellantPostcode(
        CaseDataMap caseDataMap
    ) {
        if (caseDataMap.get(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)
                .orElse(NO) == YES) {

            Optional<AddressUk> optionalAppellantAddress = caseDataMap.get(APPELLANT_ADDRESS);

            if (optionalAppellantAddress.isPresent()) {

                AddressUk appellantAddress = optionalAppellantAddress.get();

                return appellantAddress.getPostCode();
            }
        }

        return Optional.empty();
    }

    private void trySetHearingCentreFromPostcode(
        CaseDataMap caseDataMap
    ) {
        Optional<String> optionalAppellantPostcode = getAppellantPostcode(caseDataMap);

        if (optionalAppellantPostcode.isPresent()) {

            String appellantPostcode = optionalAppellantPostcode.get();
            caseDataMap.write(HEARING_CENTRE,
                hearingCentreFinder.find(appellantPostcode)
            );

        } else {
            caseDataMap.write(HEARING_CENTRE, hearingCentreFinder.getDefaultHearingCentre());
        }
    }
}
