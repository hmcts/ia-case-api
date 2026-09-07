package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.clearNlrFields;

@Component
@Slf4j
public class RevokeCitizenAccessHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final CcdDataService ccdDataService;

    public RevokeCitizenAccessHandler(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REVOKE_CITIZEN_ACCESS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        DynamicList revokeAccessDl = asylumCase.read(
                AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException(
                "Dynamic list of users to revoke access from is not present."));
        String idamId = revokeAccessDl.getValue().getCode().split(":")[0];

        long caseId = callback.getCaseDetails().getId();

        ccdDataService.revokeUserAccessToCase(caseId, idamId);
        asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getIdamId)
            .filter(nlrIdamId -> nlrIdamId.equals(idamId))
            .ifPresent(nlrIdamId -> {
                asylumCase.write(HAS_NON_LEGAL_REP, YesOrNo.NO);
                clearNlrFields(asylumCase);
            });

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}