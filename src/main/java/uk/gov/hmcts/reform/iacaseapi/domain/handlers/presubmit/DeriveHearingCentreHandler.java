package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
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

        List<Event> validEvents = Arrays.asList(Event.SUBMIT_APPEAL, Event.EDIT_APPEAL_AFTER_SUBMIT);
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && validEvents.contains(callback.getEvent());
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

        if (!asylumCase.read(HEARING_CENTRE).isPresent()
            || Event.EDIT_APPEAL_AFTER_SUBMIT.equals(callback.getEvent())) {

            trySetHearingCentreFromPostcode(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<String> getAppellantPostcode(
        AsylumCase asylumCase
    ) {
        if (asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)
                .orElse(NO) == YES) {

            Optional<AddressUk> optionalAppellantAddress = asylumCase.read(APPELLANT_ADDRESS);

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
            HearingCentre hearingCentre = hearingCentreFinder.find(appellantPostcode);

            asylumCase.write(HEARING_CENTRE, hearingCentre);
            asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);

        } else {
            asylumCase.write(HEARING_CENTRE, hearingCentreFinder.getDefaultHearingCentre());
            asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentreFinder.getDefaultHearingCentre());
        }
    }
}
