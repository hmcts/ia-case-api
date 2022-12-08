package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class LetterSentOrReceivedHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.MID_EVENT || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && !HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData())
               && Arrays.asList(
            Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT
        ).contains(callback.getEvent());
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

        YesOrNo isOutOfCountryEnabled = asylumCase
                .read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class)
                .orElseThrow(() -> new IllegalArgumentException("isOutOfCountryEnabled is missing"));

        YesOrNo appellantInUk = asylumCase
                .read(APPELLANT_IN_UK, YesOrNo.class)
                .orElseThrow(() -> new IllegalArgumentException("appellantInUk is missing"));

        Optional<YesOrNo> isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class);
        Optional<YesOrNo> isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);
        Optional<YesOrNo> appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class);
        if (isAgeAssessmentAppeal.equals(Optional.of(NO))) {
            // Set the values only for non age assessment appeals. For age assessment, we have separate field - DATE_ON_DECISION_LETTER
            if ((isOutOfCountryEnabled.equals(YES) && appellantInUk.equals(NO))
                || isAcceleratedDetainedAppeal.equals(Optional.of(YesOrNo.YES))) {
                asylumCase.write(LETTER_SENT_OR_RECEIVED, "Received");
            } else if ((appellantInUk.equals(YES) && appellantInDetention.equals(Optional.of(YesOrNo.NO)))
                || (appellantInUk.equals(YES) && appellantInDetention.equals(Optional.of(YesOrNo.YES))
                && isAcceleratedDetainedAppeal.equals(Optional.of(NO)))) {
                asylumCase.write(LETTER_SENT_OR_RECEIVED, "Sent");
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
