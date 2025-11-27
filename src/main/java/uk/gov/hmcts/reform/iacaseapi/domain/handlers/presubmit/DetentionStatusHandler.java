package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class DetentionStatusHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.EDIT_APPEAL
                || callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<YesOrNo> appellantInDetentionOpt =
                asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class);
        Optional<YesOrNo> isAdaOpt =
                asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);
        Event event = callback.getEvent();

        if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL)
                && appellantInDetentionOpt.isEmpty()) {
            asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.NO);
            appellantInDetentionOpt = Optional.of(YesOrNo.NO);
        }

        if (appellantInDetentionOpt.isPresent() && isAdaOpt.isPresent()) {
            YesOrNo appellantInDetention = appellantInDetentionOpt.get();
            YesOrNo isAda = isAdaOpt.get();

        if (appellantInDetention == YesOrNo.YES && isAda ==  YesOrNo.YES) {
            asylumCase.write(DETENTION_STATUS, DetentionStatus.ACCELERATED);
        } else if (appellantInDetention == YesOrNo.YES && isAda == YesOrNo.NO) {
                asylumCase.write(DETENTION_STATUS, DetentionStatus.DETAINED);
            }
        }

        if (event == Event.MARK_APPEAL_AS_DETAINED && isAdaOpt.isPresent()) {
            asylumCase.write(DETENTION_STATUS, DetentionStatus.DETAINED);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);

    }

}
