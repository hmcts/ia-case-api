package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

import java.util.List;

@Slf4j
@Component
public class RemoveAppealFromOnlineConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final RoleAssignmentService roleAssignmentService;

    public RemoveAppealFromOnlineConfirmation(RoleAssignmentService roleAssignmentService) {
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {

        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        String caseId = String.valueOf(callback.getCaseDetails().getId());
        roleAssignmentService.removeCaseManagerRole(caseId, List.of(UserRole.CASE_OFFICER.name()), List.of(RoleCategory.LEGAL_OPERATIONS));

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've removed this appeal from the online service");
        postSubmitResponse.setConfirmationBody(
            "## Do this next\n"
            + "You now need to:</br>"
            + "1.Contact the appellant and the respondent to inform them that the case will proceed offline.</br>"
            + "2.Save all files associated with the appeal to the shared drive.</br>"
            + "3.Email a link to the saved files with the appeal reference number to: BAUArnhemHouse@justice.gov.uk"
        );

        return postSubmitResponse;
    }

}
