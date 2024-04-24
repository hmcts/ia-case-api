package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class ListCasePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public ListCasePreparer(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.LIST_CASE;
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

        Optional<HearingCentre> maybeHearingCentre =
            asylumCase.read(HEARING_CENTRE);

        if (asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.NO)).orElse(true)) {

            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. You cannot list the case until the hearing requirements have been reviewed.");
            return asylumCasePreSubmitCallbackResponse;
        }

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {
            asylumCase.clear(LIST_CASE_HEARING_CENTRE);
            asylumCase.clear(LIST_CASE_HEARING_CENTRE_ADDRESS);
            asylumCase.clear(LIST_CASE_HEARING_DATE);
            asylumCase.clear(LIST_CASE_HEARING_LENGTH);
            asylumCase.clear(LISTING_LENGTH);
        } else {

            maybeHearingCentre.ifPresent(hearingCentre -> {
                if (hearingCentre.equals(HearingCentre.GLASGOW)) {
                    asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
                } else {
                    asylumCase.write(LIST_CASE_HEARING_CENTRE, hearingCentre);
                }
            });
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
