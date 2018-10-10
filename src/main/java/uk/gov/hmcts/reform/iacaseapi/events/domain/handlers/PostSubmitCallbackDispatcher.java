package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;

@Component
public class PostSubmitCallbackDispatcher<T extends CaseData> {

    private final List<PostSubmitCallbackHandler<T>> postSubmitHandlers;

    public PostSubmitCallbackDispatcher(
        @Autowired List<PostSubmitCallbackHandler<T>> postSubmitHandlers
    ) {
        this.postSubmitHandlers = postSubmitHandlers;
    }

    public PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<T> callback
    ) {
        if (callbackStage != CallbackStage.SUBMITTED) {
            throw new IllegalArgumentException("callbackStage is not post submit");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        for (PostSubmitCallbackHandler<T> postSubmitHandler : postSubmitHandlers) {

            if (postSubmitHandler.canHandle(callbackStage, callback)) {

                PostSubmitCallbackResponse postSubmitResponseFromHandler =
                    postSubmitHandler.handle(callbackStage, callback);

                postSubmitResponse.setConfirmationHeader(
                    postSubmitResponseFromHandler
                        .getConfirmationHeader()
                        .orElse(null)
                );

                postSubmitResponse.setConfirmationBody(
                    postSubmitResponseFromHandler
                        .getConfirmationBody()
                        .orElse(null)
                );
            }
        }

        return postSubmitResponse;
    }
}
