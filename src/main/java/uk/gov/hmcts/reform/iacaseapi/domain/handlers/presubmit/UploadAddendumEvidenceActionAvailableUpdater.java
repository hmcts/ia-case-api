package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UploadAddendumEvidenceActionAvailableUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDetails<AsylumCase> caseDetails =
            callback
                .getCaseDetails();

        AsylumCase asylumCase = caseDetails.getCaseData();

        if (Arrays.asList(
            State.PRE_HEARING,
            State.DECISION,
            State.DECIDED
        ).contains(caseDetails.getState())) {
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_ACTION_AVAILABLE, YesOrNo.YES);
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP_ACTION_AVAILABLE, YesOrNo.YES);
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE_ACTION_AVAILABLE, YesOrNo.YES);
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER_ACTION_AVAILABLE, YesOrNo.YES);
            asylumCase.write(MARK_ADDENDUM_EVIDENCE_AS_REVIEWED_ACTION_AVAILABLE, YesOrNo.YES);
        } else {
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_ACTION_AVAILABLE, YesOrNo.NO);
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP_ACTION_AVAILABLE, YesOrNo.NO);
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE_ACTION_AVAILABLE, YesOrNo.NO);
            asylumCase.write(UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER_ACTION_AVAILABLE, YesOrNo.NO);
            asylumCase.write(MARK_ADDENDUM_EVIDENCE_AS_REVIEWED_ACTION_AVAILABLE, YesOrNo.NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
