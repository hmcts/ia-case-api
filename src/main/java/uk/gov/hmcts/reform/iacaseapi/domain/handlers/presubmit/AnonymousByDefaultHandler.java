package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ANONYMITY_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGACY_CASE_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.LegacyCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseFlagAppender;

@Component
class AnonymousByDefaultHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CaseFlagAppender caseFlagAppender;

    AnonymousByDefaultHandler(CaseFlagAppender caseFlagAppender) {
        this.caseFlagAppender = caseFlagAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (callback.getEvent() == SUBMIT_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class).orElse(null);

        if (isAPorRPappeal(appealType)) {
            setAnonymityFlag(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isAPorRPappeal(AppealType appealType) {
        return appealType != null
                && (appealType == AppealType.PA
                || appealType == AppealType.RP);
    }

    private List<IdValue<LegacyCaseFlag>> getExistingCaseFlags(AsylumCase asylumCase) {
        Optional<List<IdValue<LegacyCaseFlag>>> maybeExistingCaseFlags = asylumCase.read(LEGACY_CASE_FLAGS);
        return maybeExistingCaseFlags.orElse(Collections.emptyList());
    }

    private void setAnonymityFlag(AsylumCase asylumCase) {
        asylumCase.write(LEGACY_CASE_FLAGS, caseFlagAppender.append(
                getExistingCaseFlags(asylumCase),
                CaseFlagType.ANONYMITY, ""
        ));
        asylumCase.write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
    }
}
