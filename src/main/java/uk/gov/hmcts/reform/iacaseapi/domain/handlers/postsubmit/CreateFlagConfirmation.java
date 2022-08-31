package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;

import com.google.common.collect.Maps;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;


@Component
public class CreateFlagConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;
    private String roleOnCase;

    public CreateFlagConfirmation(CcdSupplementaryUpdater ccdSupplementaryUpdater,
                                  @Value("${role_on_case}") String roleOnCase
    ) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
        this.roleOnCase = roleOnCase;
    }

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.CREATE_FLAG;
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        final PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        final String appellantNameForDisplay =
                asylumCase
                        .read(APPELLANT_NAME_FOR_DISPLAY, String.class)
                        .orElseThrow(() -> new IllegalStateException("appellantNameForDisplay is not present"));


        Map<String, Object> coreData = Maps.newHashMap();
        coreData.put("partyName", appellantNameForDisplay);
        coreData.put("roleOnCase", roleOnCase);

        ccdSupplementaryUpdater.setSupplementaryValues(callback, coreData);

        //postSubmitResponse.setConfirmationHeader("# You've flagged this case");
        //postSubmitResponse.setConfirmationBody(
        //        "#### What happens next\r\n\r\n"
        //                + "This flag will only be visible to the Tribunal. The case will proceed as usual."
        //);
        return postSubmitResponse;
    }
}
