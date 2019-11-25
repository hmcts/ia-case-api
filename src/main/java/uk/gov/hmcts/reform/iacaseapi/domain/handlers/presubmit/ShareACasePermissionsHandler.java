package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Service
public class ShareACasePermissionsHandler implements PreSubmitCallbackHandler<AsylumCase> {


    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SHARE_A_CASE;

    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        canHandle(callbackStage, callback);

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<DynamicList> orgListOfUsers = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        Optional<String> sharedColleague = asylumCase.read(AsylumCaseFieldDefinition.SHARED_WITH_COLLEAGUE_ID);

        final Value selectedValue = orgListOfUsers
            .orElseThrow(() -> new IllegalStateException("No selected value from Dynamic List for OrgListOfUsers field"))
            .getValue();

        log.info("selected: code {}, label: {}", selectedValue.getCode(), selectedValue.getLabel());
        log.info("SharedWithColleague: {}", sharedColleague.orElse(""));

        //Data Needed for CCD update
        long caseId = callback.getCaseDetails().getId();
        String jurisdiction = callback.getCaseDetails().getJurisdiction();
        String caseTypeId = "Asylum"; //TODO should come from CCD callback

        //TODO call CCD : http://ccd-data-store-api-demo.service.core-compute-demo.internal/caseworkers/${idOfUserWhoGrantsAccess}/jurisdictions/${jurisdiction}/case-types/${caseType}/cases/${caseId}/users`
        //TODO Handle any errors or respond

        return new PreSubmitCallbackResponse<>(asylumCase);

    }
}
