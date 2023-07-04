package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_S94B_STATUS_UPDATABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_S94B_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class S94bStatusHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String ERROR = "\'Update s94b status\' not available for this appeal type";

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return eventEnablingFlagToBeSet(callbackStage, callback)
               || errorToBeAdded(callbackStage, callback);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback
            .getCaseDetails()
            .getCaseData();

        boolean isHu = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .map(appealType -> appealType.equals(HU))
            .orElse(false);

        boolean isEa = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .map(appealType -> appealType.equals(EA))
            .orElse(false);

        boolean isAppellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
            .map(appellantInUk -> appellantInUk.equals(YES))
            .orElse(false);

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (isHu || (isEa && !isAppellantInUk)) {

            if (eventEnablingFlagToBeSet(callbackStage, callback)) {

                populateS94bStatusIfEmpty(asylumCase);
                asylumCase.write(IS_S94B_STATUS_UPDATABLE, YES);
            }
        } else {

            if (eventEnablingFlagToBeSet(callbackStage, callback)) {

                asylumCase.write(IS_S94B_STATUS_UPDATABLE, NO);
            }

            // For in-flight cases which can't rely on the flag field being populated
            if (errorToBeAdded(callbackStage, callback)) {
                response.addError(ERROR);
            }
        }

        return response;
    }

    private boolean eventEnablingFlagToBeSet(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == ABOUT_TO_SUBMIT
               && Set.of(START_APPEAL, EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
    }

    private boolean errorToBeAdded(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == ABOUT_TO_START
                && callback.getEvent() == UPDATE_S94B_STATUS;
    }

    private void populateS94bStatusIfEmpty(AsylumCase asylumCase) {
        Optional<YesOrNo> s94bStatus = asylumCase.read(S94B_STATUS, YesOrNo.class);

        if (s94bStatus.isEmpty()) {
            asylumCase.write(S94B_STATUS, NO);
        }
    }

}

