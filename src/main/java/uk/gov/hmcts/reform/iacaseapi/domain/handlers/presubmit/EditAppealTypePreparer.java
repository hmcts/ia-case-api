package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NABA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealTypeForDisplay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EditAppealTypePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.EDIT_APPEAL);
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
        // Pre NABA Definition release, there is no population of AppealTypeForDisplay. So no change needed.
        Optional<YesOrNo> isNabaEnabled = asylumCase.read(IS_NABA_ENABLED, YesOrNo.class);
        if (isNabaEnabled.equals(Optional.of(NO))) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        // After release of NABA, the pre-NABA cases will not have APPEAL_TYPE_FOR_DISPLAY populated, so when they get
        // to select appeal type screen (via EditAppeal event), it will not be pre-populated. This preparer will
        // make sure APPEAL_TYPE_FOR_DISPLAY is written into from APPEAL_TYPE.
        YesOrNo ageAssessment = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO);

        if (ageAssessment.equals(NO)
                && asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class).isEmpty()) {
            AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                    .orElseThrow(() -> new IllegalStateException("Appeal type not present"));
            asylumCase.write(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.from(appealType.getValue()));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
