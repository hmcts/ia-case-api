package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RESTORE_STATE_FROM_ADJOURN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;

@Slf4j
@Component
public class RestoreFromAdjournStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(RESTORE_STATE_FROM_ADJOURN);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback,
                                                        PreSubmitCallbackResponse<AsylumCase> callbackResponse) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String previousHearingDate = asylumCase.read(DATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("dateBeforeAdjournWithoutDate is missing."));


        asylumCase.write(LIST_CASE_HEARING_DATE, previousHearingDate);

        asylumCase.clear(DATE_BEFORE_ADJOURN_WITHOUT_DATE);
        asylumCase.clear(ADJOURN_HEARING_WITHOUT_DATE_REASONS);

        String maybePreviousStateId = asylumCase.read(STATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("stateBeforeAdjournWithoutDate is missing."));

        State previousState = State.valueOf(LOWER_CAMEL.to(UPPER_UNDERSCORE, maybePreviousStateId));

        asylumCase.clear(STATE_BEFORE_ADJOURN_WITHOUT_DATE);

        return new PreSubmitCallbackResponse<>(asylumCase, previousState);
    }
}
