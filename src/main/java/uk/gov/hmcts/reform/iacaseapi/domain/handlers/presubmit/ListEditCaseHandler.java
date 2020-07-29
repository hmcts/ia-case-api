package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;


@Component
public class ListEditCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;

    public ListEditCaseHandler(HearingCentreFinder hearingCentreFinder) {
        this.hearingCentreFinder = hearingCentreFinder;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.LIST_CASE || callback.getEvent() == Event.EDIT_CASE_LISTING);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        HearingCentre hearingCentre =
            asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.TAYLOR_HOUSE);

        if (!hearingCentreFinder.hearingCentreIsActive(hearingCentre)) {
            asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.TAYLOR_HOUSE);
            asylumCase.write(HEARING_CENTRE, HearingCentre.TAYLOR_HOUSE);
        } else {
            if (!hearingCentreFinder.isListingOnlyHearingCentre(hearingCentre)){
                //Should also update the designated hearing centre
                asylumCase.write(HEARING_CENTRE, hearingCentre);
            }
        }

        asylumCase.clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        asylumCase.clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
