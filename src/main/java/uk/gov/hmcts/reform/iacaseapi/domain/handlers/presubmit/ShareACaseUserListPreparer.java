package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@Slf4j
@Service
public class ShareACaseUserListPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final ProfessionalUsersRetriever professionalUsersRetriever;

    private final UserDetailsProvider userDetailsProvider;

    public ShareACaseUserListPreparer(
        ProfessionalUsersRetriever professionalUsersRetriever,
        UserDetailsProvider userDetailsProvider
    ) {
        this.professionalUsersRetriever = professionalUsersRetriever;
        this.userDetailsProvider = userDetailsProvider;
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

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        return mapToAsylumCase(callback, professionalUsersRetriever.retrieve());

    }

    private PreSubmitCallbackResponse<AsylumCase> mapToAsylumCase(Callback<AsylumCase> callback,
                                                                  ProfessionalUsersResponse usersResponse) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String loggedUserEmail = userDetailsProvider.getUserDetails().getEmailAddress();

        Set<String> usersWithCaseAccess = asylumCase
            .<List<IdValue<String>>>read(AsylumCaseFieldDefinition.SHARE_A_CASE_USER_LIST)
            .orElse(emptyList())
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toSet());

        DynamicList dynamicList;

        final List<Value> values =
            usersResponse.getProfessionalUsers()
                .stream()
                .filter(professionalUser ->
                    professionalUser.getIdamStatus().equalsIgnoreCase("ACTIVE")
                )
                .filter(professionalUser ->
                    !professionalUser.getEmail().equalsIgnoreCase(loggedUserEmail)
                )
                .filter(professionalUser ->
                    !usersWithCaseAccess.contains(professionalUser.getEmail())
                )
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

        if(usersWithCaseAccess.isEmpty()) {
            asylumCase.write(AsylumCaseFieldDefinition.SHARE_A_CASE_USER_LIST_READ_ONLY, "- None");

        } else {
            asylumCase.write(
                AsylumCaseFieldDefinition.SHARE_A_CASE_USER_LIST_READ_ONLY,
                Joiner.on("\n").join(
                    usersWithCaseAccess
                        .stream()
                        .map(user -> "- " + user)
                        .collect(Collectors.toSet())
                )
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);

    }
}
