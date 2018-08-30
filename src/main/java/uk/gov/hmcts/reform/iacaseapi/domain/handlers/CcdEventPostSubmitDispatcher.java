package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

@Component
public class CcdEventPostSubmitDispatcher<T extends CaseData> {

    private final List<CcdEventPostSubmitHandler<T>> postSubmitHandlers;

    public CcdEventPostSubmitDispatcher(
        @Autowired List<CcdEventPostSubmitHandler<T>> postSubmitHandlers
    ) {
        this.postSubmitHandlers = postSubmitHandlers;
    }

    public CcdEventPostSubmitResponse handle(
        Stage stage,
        CcdEvent<T> ccdEvent
    ) {
        CcdEventPostSubmitResponse postSubmitResponse =
            new CcdEventPostSubmitResponse();

        for (CcdEventPostSubmitHandler<T> postSubmitHandler : postSubmitHandlers) {

            if (postSubmitHandler.canHandle(stage, ccdEvent)) {

                CcdEventPostSubmitResponse postSubmitResponseFromHandler =
                    postSubmitHandler.handle(stage, ccdEvent);

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
