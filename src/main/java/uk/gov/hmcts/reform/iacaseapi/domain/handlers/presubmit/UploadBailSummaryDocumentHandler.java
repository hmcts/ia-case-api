package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.BailCaseUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadBailSummaryDocumentHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadBailSummaryDocumentHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<DocumentWithDescription>>> maybeBailSummary = bailCase.read(UPLOAD_BAIL_SUMMARY_DOCS);

        if (maybeBailSummary.isPresent()) {
            List<DocumentWithMetadata> bailSummary =
                maybeBailSummary
                    .orElseThrow(() -> new IllegalStateException("bailSummary is not present"))
                    .stream()
                    .map(IdValue::getValue)
                    .map(document -> documentReceiver.tryReceive(document, DocumentTag.BAIL_SUMMARY))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingHomeOfficeDocuments =
                bailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA);

            final List<IdValue<DocumentWithMetadata>> existingHomeOfficeDocuments =
                maybeExistingHomeOfficeDocuments.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allBailSummaryDocuments =
                documentsAppender.append(existingHomeOfficeDocuments, bailSummary);

            bailCase.write(HOME_OFFICE_DOCUMENTS_WITH_METADATA, allBailSummaryDocuments);
            bailCase.clear(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE);
            bailCase.clear(HAS_CASE_BEEN_FORCED_TO_HEARING);
        }

        // If the case is progressed past Bail summary, then default IMA selection is NO
        YesOrNo hoSelectedIma = bailCase.read(HO_SELECT_IMA_STATUS, YesOrNo.class).orElse(YesOrNo.NO);
        bailCase.write(HO_HAS_IMA_STATUS, BailCaseUtils.isImaEnabled(bailCase) ? hoSelectedIma : YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
