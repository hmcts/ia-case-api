package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdUpdater;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ShareACasePermissionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private CcdUpdater ccdUpdater;

    public ShareACasePermissionsHandler(CcdUpdater ccdUpdater) {
        this.ccdUpdater = ccdUpdater;
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

        ccdUpdater.updatePermissions(callback);

        List<IdValue<String>> usersWithAccess = asylumCase
            .<List<IdValue<String>>>read(AsylumCaseFieldDefinition.SHARE_A_CASE_USER_LIST)
            .orElse(new ArrayList<>());

        usersWithAccess.add(
            new IdValue<>(
                String.valueOf(usersWithAccess.size() + 1),
                asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS, DynamicList.class)
                    .map(users -> users.getValue().getLabel())
                    .orElseThrow(() -> new IllegalStateException("user to share is mandatory"))
            )
        );

        asylumCase.write(AsylumCaseFieldDefinition.SHARE_A_CASE_USER_LIST, usersWithAccess);


        asylumCase.clear(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        asylumCase.clear(AsylumCaseFieldDefinition.SHARE_A_CASE_USER_LIST_READ_ONLY);

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());

    }
}
