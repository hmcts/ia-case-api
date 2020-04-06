package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_DOCUMENTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
@Slf4j
public class EditDocsAuditLogHandler implements PostSubmitCallbackHandler<AsylumCase> {

    @Autowired
    private UserDetailsProvider userDetailsProvider;

    @Autowired
    private EditDocsAuditService editDocsAuditService;

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.EDIT_DOCUMENTS;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        log.info("Edit Document audit logs...");
        log.info("CCD case id: {}", callback.getCaseDetails().getId());
        log.info("Delete/Update document ids: {}", getDeletedDocIds(callback.getCaseDetails(),
            callback.getCaseDetailsBefore().orElse(null)));
        log.info("IDAM User id: {}", userDetailsProvider.getUserDetails().getId());
        return new PostSubmitCallbackResponse();
    }

    private List<String> getDeletedDocIds(CaseDetails<AsylumCase> caseDetails,
                                          CaseDetails<AsylumCase> caseDetailsBefore) {
        if (caseDetailsBefore == null) {
            return Collections.emptyList();
        }
        List<String> docIds = new ArrayList<>();
        AsylumCase asylumCase = caseDetails.getCaseData();
        AsylumCase asylumCaseBefore = caseDetailsBefore.getCaseData();
        getListOfDocumentFields().forEach(field -> docIds.addAll(
            editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(asylumCase, asylumCaseBefore, field)));
        return docIds;
    }

    private List<AsylumCaseFieldDefinition> getListOfDocumentFields() {
        return Arrays.asList(ADDITIONAL_EVIDENCE_DOCUMENTS, TRIBUNAL_DOCUMENTS, HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS, ADDENDUM_EVIDENCE_DOCUMENTS, RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS, FINAL_DECISION_AND_REASONS_DOCUMENTS, HEARING_RECORDING_DOCUMENTS);
    }

}
