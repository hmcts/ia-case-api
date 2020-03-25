package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOW_PAGE_FLAG;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RequestCaseBuildingMidHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.REQUEST_CASE_BUILDING;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.write(SHOW_PAGE_FLAG, showReasonToForceProgressionPageIfHomeOfficeBundledIsNotPresent(asylumCase));
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private YesOrNo showReasonToForceProgressionPageIfHomeOfficeBundledIsNotPresent(AsylumCase asylumCase) {
        return isHomeOfficeBundledPresent(asylumCase) ? YesOrNo.NO : YesOrNo.YES;
    }

    private boolean isHomeOfficeBundledPresent(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> respondentDocumentsOptional =
            asylumCase.read(RESPONDENT_DOCUMENTS);
        if (respondentDocumentsOptional.isPresent()) {
            List<IdValue<DocumentWithMetadata>> respondentDocuments = respondentDocumentsOptional.get();
            return !respondentDocuments.isEmpty();
        }
        return false;
    }
}
