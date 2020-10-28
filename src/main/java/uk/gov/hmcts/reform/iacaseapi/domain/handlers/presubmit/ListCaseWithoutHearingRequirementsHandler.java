package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@Component
public class ListCaseWithoutHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public ListCaseWithoutHearingRequirementsHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
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

        // bringing the status as it would have gone the normal way and differentiating with CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS
        // this ensures all the functionality using the flags work as expected
        asylumCase.write(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.YES);
        asylumCase.write(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);

        if (featureToggler.getValue("reheard-feature", false)
            && asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)) {
            asylumCase.write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
            asylumCase.write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
            asylumCase.write(PREVIOUS_HEARING_DETAILS_VISIBLE, YesOrNo.NO);
            clearAttendanceAndDurationFields(asylumCase);
        } else {
            asylumCase.write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    protected void clearAttendanceAndDurationFields(AsylumCase asylumCase) {
        asylumCase.clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
        asylumCase.clear(ATTENDING_TCW);
        asylumCase.clear(ATTENDING_JUDGE);
        asylumCase.clear(ATTENDING_APPELLANT);
        asylumCase.clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        asylumCase.clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        asylumCase.clear(ACTUAL_CASE_HEARING_LENGTH);
        asylumCase.clear(HEARING_CONDUCTION_OPTIONS);
        asylumCase.clear(HEARING_RECORDING_DOCUMENTS);
    }
}

