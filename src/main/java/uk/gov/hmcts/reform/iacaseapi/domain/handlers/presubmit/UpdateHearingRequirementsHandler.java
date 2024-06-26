package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.populateAppellantInterpreterLanguageFieldsIfRequired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;

@Component
public class UpdateHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    private final FeatureToggler featureToggler;

    public UpdateHearingRequirementsHandler(
        PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender,
        FeatureToggler featureToggler
    ) {
        this.previousRequirementsAndRequestsAppender = previousRequirementsAndRequestsAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPDATE_HEARING_REQUIREMENTS;
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

        populateAppellantInterpreterLanguageFieldsIfRequired(asylumCase);

        List<WitnessDetails> witnessDetails = asylumCase.<List<IdValue<WitnessDetails>>>read(WITNESS_DETAILS)
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList());

        asylumCase.write(WITNESS_COUNT, witnessDetails.size());

        asylumCase.write(DISABLE_OVERVIEW_PAGE, YesOrNo.YES);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
        asylumCase.write(UPDATE_HEARING_REQUIREMENTS_EXISTS, YesOrNo.YES);

        changeUpdateHearingsApplicationsToCompleted(asylumCase);
        asylumCase.clear(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS);

        // we need to clear this to reset the agreed adjustments
        asylumCase.clear(REVIEWED_HEARING_REQUIREMENTS);

        // Clear review fields once the update happens
        asylumCase.clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        asylumCase.clear(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE);
        asylumCase.clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        asylumCase.clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        asylumCase.clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        asylumCase.clear(ADDITIONAL_TRIBUNAL_RESPONSE);

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {
            previousRequirementsAndRequestsAppender.appendAndTrim(asylumCase);
        }

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        if (isAcceleratedDetainedAppeal) {
            //Set flag to Yes to enable updateHearingAdjustmentsEvent for ada cases
            asylumCase.write(ADA_HEARING_ADJUSTMENTS_UPDATABLE, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void changeUpdateHearingsApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.UPDATE_HEARING_REQUIREMENTS.toString().equals(applicationType)) {

                    return new IdValue<>(application.getId(), new Application(
                        application.getValue().getApplicationDocuments(),
                        application.getValue().getApplicationSupplier(),
                        applicationType,
                        application.getValue().getApplicationReason(),
                        application.getValue().getApplicationDate(),
                        application.getValue().getApplicationDecision(),
                        application.getValue().getApplicationDecisionReason(),
                        application.getValue().getApplicationDateOfDecision(),
                        "Completed"
                    ));
                }

                return application;
            })
            .collect(Collectors.toList())
        );
    }
}

