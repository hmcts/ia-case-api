package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Component
@RequiredArgsConstructor
public class ListCmaPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final LocationRefDataService locationRefDataService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.LIST_CMA;
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

        boolean isHearingRequirementsAvailable = asylumCase
            .read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.YES))
            .orElse(false);
        boolean isHearingRequirementsReviewed = asylumCase
            .read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.NO))
            .orElse(true);

        if (isHearingRequirementsAvailable && isHearingRequirementsReviewed) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. You cannot list the case management appointment until the hearing requirements have been reviewed.");
            return asylumCasePreSubmitCallbackResponse;
        }
        maybeHearingCentre.ifPresent(hearingCentre -> {
            asylumCase.write(LIST_CASE_HEARING_CENTRE, hearingCentre);
            asylumCase.write(LIST_CASE_HEARING_CENTRE_ADDRESS, locationRefDataService.getHearingCentreAddress(hearingCentre));
        });
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
