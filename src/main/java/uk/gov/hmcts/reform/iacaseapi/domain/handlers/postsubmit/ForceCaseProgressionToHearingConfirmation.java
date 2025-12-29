package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ForceCaseProgressionToHearingConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private static final String CONFIRMATION_HEADER = "# You have forced the case progression to Hearing";
    private static final String CONFIRMATION_BODY_LEGAL_REP = """
            #### What happens next

            The respondent and legal representative will be notified by email.""";
    private static final String CONFIRMATION_BODY_NO_LEGAL_REP = """
            #### What happens next

            The respondent will be notified by email.""";

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.FORCE_CASE_TO_HEARING;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader(CONFIRMATION_HEADER);
        postSubmitResponse.setConfirmationBody(getConfirmationBody(callback.getCaseDetails().getCaseData()));

        return postSubmitResponse;
    }

    private String getConfirmationBody(BailCase bailCaseData) {
        return isLegallyRepresented(bailCaseData) ? CONFIRMATION_BODY_LEGAL_REP : CONFIRMATION_BODY_NO_LEGAL_REP;
    }

    private boolean isLegallyRepresented(BailCase bailCaseData) {
        return YesOrNo.YES.equals(bailCaseData.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO));
    }
}
