package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdUpdater;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@Slf4j
@Service
public class ShareACasePermissionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CcdUpdater ccdUpdater;
    private final ProfessionalUsersRetriever professionalUsersRetriever;

    public ShareACasePermissionsHandler(CcdUpdater ccdUpdater, ProfessionalUsersRetriever professionalUsersRetriever) {
        this.ccdUpdater = ccdUpdater;
        this.professionalUsersRetriever = professionalUsersRetriever;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SHARE_A_CASE;

    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (!hasValidUserId(asylumCase)) {

            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
            response.addError("You can share a case only with Active Users in your Organization.");

            asylumCase.clear(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);

            return response;
        }

        ccdUpdater.updatePermissions(callback);

        asylumCase.clear(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());

    }

    private boolean hasValidUserId(AsylumCase asylumCase) {

        String userId = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException(
                AsylumCaseFieldDefinition.ORG_LIST_OF_USERS + " is empty in case data when required.")
            )
            .getValue()
            .getCode();

        return professionalUsersRetriever
            .retrieve()
            .getUsers()
            .stream()
            .anyMatch(user -> userId.equalsIgnoreCase(user.getUserIdentifier()) && user.getIdamStatus().equalsIgnoreCase("ACTIVE"));
    }
}
