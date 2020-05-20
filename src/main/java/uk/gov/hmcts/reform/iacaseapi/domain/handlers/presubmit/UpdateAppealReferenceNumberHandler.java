package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@Service
public class UpdateAppealReferenceNumberHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    public UpdateAppealReferenceNumberHandler(AppealReferenceNumberGenerator appealReferenceNumberGenerator) {
        this.appealReferenceNumberGenerator = appealReferenceNumberGenerator;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return PreSubmitCallbackStage.ABOUT_TO_SUBMIT == callbackStage
            && Event.EDIT_APPEAL_AFTER_SUBMIT.equals(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        Optional<String> existingAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);
        if (existingAppealReferenceNumber.isPresent()) {
            updateAppealReferenceNumber(asylumCase, callback.getCaseDetails().getId());
        }
        return callbackResponse;
    }

    private void updateAppealReferenceNumber(AsylumCase asylumCase, long caseId) {
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("appealType is not present"));
        String appealReferenceNumber = appealReferenceNumberGenerator.update(caseId, appealType);
        asylumCase.write(APPEAL_REFERENCE_NUMBER, appealReferenceNumber);
    }
}
