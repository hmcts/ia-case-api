package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
@Slf4j
public class EditDocsAuditLogHandler implements PostSubmitCallbackHandler<AsylumCase> {

    @Autowired
    private EditDocsAuditLogService editDocsAuditLogService;

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.EDIT_DOCUMENTS;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        long caseId = callback.getCaseDetails().getId();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        CaseDetails<AsylumCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
        AsylumCase asylumCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
        log.info("Edit Document audit logs: {}", editDocsAuditLogService
            .buildAuditDetails(caseId, asylumCase, asylumCaseBefore));
        return new PostSubmitCallbackResponse();
    }

}
