package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@Slf4j
@Service
public class ShareACaseUserListHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private ProfessionalUsersRetriever professionalUsersRetriever;

    public ShareACaseUserListHandler(ProfessionalUsersRetriever professionalUsersRetriever) {
        this.professionalUsersRetriever = professionalUsersRetriever;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.SHARE_A_CASE;

    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        canHandle(callbackStage, callback);
        return mapToAsylumCase(callback, professionalUsersRetriever.retrieve());

    }

    private PreSubmitCallbackResponse<AsylumCase> mapToAsylumCase(Callback<AsylumCase> callback,
                                                                  ProfessionalUsersResponse usersResponse) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.clear(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);

        DynamicList dynamicList;

        final List<Value> values =
            usersResponse.getProfessionalUsers()
                .stream()
                .map(professionalUser ->
                    new Value(
                        professionalUser.getUserIdentifier(),
                        professionalUser.getEmail()
                    )
                )
                .collect(Collectors.toList());

        if (!values.isEmpty()) {
            dynamicList = new DynamicList(values.get(0), values);
        } else {
            dynamicList = new DynamicList("");
        }

        asylumCase.write(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS, dynamicList);

        return new PreSubmitCallbackResponse<>(asylumCase);

    }
}
