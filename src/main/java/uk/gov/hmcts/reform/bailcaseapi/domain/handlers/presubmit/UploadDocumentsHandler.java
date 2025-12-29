package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_USER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_DOCUMENTS_SUPPLIED_BY;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@Component
public class UploadDocumentsHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadDocumentsHandler(DocumentReceiver documentReceiver,
                                  DocumentsAppender documentsAppender) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    private static final String SUPPLIED_BY_APPLICANT = "applicant";
    private static final String SUPPLIED_BY_LEGAL_REPRESENTATIVE = "legalRepresentative";
    private static final String SUPPLIED_BY_HOME_OFFICE = "homeOffice";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_DOCUMENTS;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();
        String userRole = bailCase.read(CURRENT_USER, String.class).orElse("");

        Optional<List<IdValue<DocumentWithDescription>>> maybeDocument = bailCase.read(UPLOAD_DOCUMENTS);
        Optional<String> maybeSuppliedBy = bailCase.read(UPLOAD_DOCUMENTS_SUPPLIED_BY);

        String suppliedBy = maybeSuppliedBy
            .orElse(currentUserIsLegalRep(userRole)
                        ? "Legal Representative"
                        : currentUserIsHomeOfficer(userRole)
                ? "Home Office"
                : "");

        if (maybeDocument.isPresent()) {
            List<DocumentWithMetadata> document =
                maybeDocument
                    .orElseThrow(() -> new IllegalStateException("document is not present"))
                    .stream()
                    .map(IdValue::getValue)
                    .map(doc -> documentReceiver.tryReceive(doc, DocumentTag.UPLOAD_DOCUMENT))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            BailCaseFieldDefinition appropriateCollectionForUserRole =
                selectAppropriateDocumentsCollection(userRole, suppliedBy);

            Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingDocumentsCollection =
                bailCase.read(appropriateCollectionForUserRole);

            final List<IdValue<DocumentWithMetadata>> existingDocuments =
                maybeExistingDocumentsCollection.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allDocuments =
                documentsAppender.append(existingDocuments, document);

            bailCase.write(appropriateCollectionForUserRole, allDocuments);

            bailCase.clear(UPLOAD_DOCUMENTS);
            bailCase.clear(UPLOAD_DOCUMENTS_SUPPLIED_BY);
            bailCase.clear(CURRENT_USER);
        }
        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private boolean currentUserIsLegalRep(String userRole) {
        return userRole.equals(UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
    }

    private boolean currentUserIsHomeOfficer(String userRole) {
        return userRole.equals(UserRoleLabel.HOME_OFFICE_BAIL.toString());
    }

    private boolean currentUserIsAdminOrJudge(String userRole) {
        return userRole.equals(UserRoleLabel.ADMIN_OFFICER.toString())
            || userRole.equals(UserRoleLabel.JUDGE.toString());
    }

    private BailCaseFieldDefinition selectAppropriateDocumentsCollection(String userRole, String suppliedBy) {
        BailCaseFieldDefinition appropriateDocumentsCollection = null;

        if (currentUserIsHomeOfficer(userRole)) {
            appropriateDocumentsCollection = HOME_OFFICE_DOCUMENTS_WITH_METADATA;
        }
        if (currentUserIsLegalRep(userRole)) {
            appropriateDocumentsCollection = APPLICANT_DOCUMENTS_WITH_METADATA;
        }
        if (currentUserIsAdminOrJudge(userRole)) {
            appropriateDocumentsCollection = suppliedBy.equals(SUPPLIED_BY_APPLICANT)
                                             || suppliedBy.equals(SUPPLIED_BY_LEGAL_REPRESENTATIVE)
                ? APPLICANT_DOCUMENTS_WITH_METADATA
                : suppliedBy.equals(SUPPLIED_BY_HOME_OFFICE)
                ? HOME_OFFICE_DOCUMENTS_WITH_METADATA
                : null;
        }

        if (appropriateDocumentsCollection == null) {
            throw new IllegalStateException("Unable to determine the supplier of the document");
        }

        return appropriateDocumentsCollection;
    }

}
