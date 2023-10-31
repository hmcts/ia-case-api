package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadAppealFormHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadAppealFormHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);

        if (isAdmin.equals(YesOrNo.YES) && !HandlerUtils.isEjpCase(asylumCase)) {
            String appellantName = asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElse("");
            String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse("");
            String appealFormSuffix = "appeal form";

            Optional<List<IdValue<DocumentWithDescription>>> maybeAppealForm =
                    asylumCase.read(UPLOAD_THE_APPEAL_FORM_DOCS);

            List<IdValue<DocumentWithDescription>> appealFormDocs =
                    maybeAppealForm.orElseThrow(() -> new IllegalStateException("appealForm is not present"));

            if (maybeAppealForm.isPresent()) {
                int docNum = 0;
                for (IdValue<DocumentWithDescription> appealFormDoc : appealFormDocs) {
                    docNum++;

                    int finalDocNum = docNum;

                    appealFormDoc.getValue().getDocument().ifPresent(document -> {
                        String fileExtension = FilenameUtils.getExtension(document.getDocumentFilename());
                        document.setDocumentFilename(appealReferenceNumber
                                                     + "-"
                                                     + appellantName
                                                     + "-"
                                                     + appealFormSuffix
                                                     + finalDocNum
                                                     + "."
                                                     + fileExtension);
                    });
                }

                List<DocumentWithMetadata> appealForms =
                        appealFormDocs
                                .stream()
                                .map(IdValue::getValue)
                                .map(document -> documentReceiver.tryReceive(document, DocumentTag.APPEAL_FORM))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList());

                Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingTribunalDocuments =
                        asylumCase.read(TRIBUNAL_DOCUMENTS);

                List<IdValue<DocumentWithMetadata>> existingTribunalDocuments =
                        maybeExistingTribunalDocuments.orElse(emptyList());

                if (!appealForms.isEmpty()) {
                    List<IdValue<DocumentWithMetadata>> allTribunalDocuments =
                            documentsAppender.prepend(existingTribunalDocuments, appealForms);
                    asylumCase.write(TRIBUNAL_DOCUMENTS, allTribunalDocuments);
                }

            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
