package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_EDIT_LISTING_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AMEND_RESPONSE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_RESPONSE_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_TRANSFERRED_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEW_HOME_OFFICE_RESPONSE_BY_LEGAL_REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEW_RESPONSE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewAmendDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Arrays.asList(
                Event.REQUEST_RESPONSE_REVIEW,
                Event.REQUEST_RESPONSE_AMEND
            ).contains(callback.getEvent());
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

        if (callback.getEvent().equals(Event.REQUEST_RESPONSE_REVIEW)) {
            asylumCase.write(REVIEW_RESPONSE_ACTION_AVAILABLE, YesOrNo.NO);
            asylumCase.write(AMEND_RESPONSE_ACTION_AVAILABLE, YesOrNo.YES);
            asylumCase.write(REVIEW_HOME_OFFICE_RESPONSE_BY_LEGAL_REP, YesOrNo.YES);

            if (appealTransferredOutOfAda(asylumCase)) {
                asylumCase.write(ADA_EDIT_LISTING_AVAILABLE, YES);

                // no need to review hearing requirements if submitted when case was ADA
                if (hasSubmittedAdaHearingRequirements(asylumCase)) {
                    asylumCase.write(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);
                }
            }
        } else {
            asylumCase.write(REVIEW_RESPONSE_ACTION_AVAILABLE, YesOrNo.YES);
            asylumCase.write(AMEND_RESPONSE_ACTION_AVAILABLE, YesOrNo.NO);
            asylumCase.write(APPEAL_RESPONSE_AVAILABLE, YesOrNo.NO);
            asylumCase.write(REVIEW_HOME_OFFICE_RESPONSE_BY_LEGAL_REP, YesOrNo.NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean hasSubmittedAdaHearingRequirements(AsylumCase asylumCase) {
        return asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YES))
            .orElse(false);
    }

    private boolean appealTransferredOutOfAda(AsylumCase asylumCase) {
        return asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YES))
            .orElse(false);
    }
}
