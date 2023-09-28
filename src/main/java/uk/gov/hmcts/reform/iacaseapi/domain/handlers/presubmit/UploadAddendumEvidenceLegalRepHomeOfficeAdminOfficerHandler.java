package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;


@Component
public class UploadAddendumEvidenceLegalRepHomeOfficeAdminOfficerHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadAddendumEvidenceLegalRepHomeOfficeAdminOfficerHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE
                   || callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP
                   || callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String party = getParty(callback.getEvent(), asylumCase);

        List<DocumentWithMetadata> addendumEvidenceDocuments =
            asylumCase
                .<List<IdValue<DocumentWithDescription>>>read(ADDENDUM_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("additionalEvidence is not present"))
                .stream()
                .map(IdValue::<DocumentWithDescription>getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.ADDENDUM_EVIDENCE, party))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(doc -> callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
                        ? new DocumentWithMetadata(doc.getDocument(), doc.getDescription(), doc.getDateUploaded(), doc.getTag(), doc.getSuppliedBy(), "TCW")
                        : doc)
                .collect(Collectors.toList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceDocuments =
            asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> existingAdditionalEvidenceDocuments =
            maybeExistingAdditionalEvidenceDocuments.orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allAddendumEvidenceDocuments =
            documentsAppender.append(existingAdditionalEvidenceDocuments, addendumEvidenceDocuments);

        asylumCase.write(ADDENDUM_EVIDENCE_DOCUMENTS, allAddendumEvidenceDocuments);

        asylumCase.clear(ADDENDUM_EVIDENCE);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getParty(Event event, AsylumCase asylumCase) {

        String party;
        switch (event) {
            case UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE:
                party = "The respondent";
                break;
            case UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP:
                party = "The appellant";
                break;
            case UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER:
                party = asylumCase
                    .<String>read(IS_APPELLANT_RESPONDENT)
                    .orElseThrow(() -> new IllegalStateException("isAppellantRespondent is not present"));
                asylumCase.clear(IS_APPELLANT_RESPONDENT);
                break;
            default:
                throw new IllegalArgumentException("Cannot workout party from event: " + event);
        }
        return party;
    }
}
