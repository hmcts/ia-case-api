package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class BuildCaseHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public BuildCaseHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.BUILD_CASE;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        final Document caseArgumentDocument =
            CaseDataMap
                .getCaseArgumentDocument()
                .orElseThrow(() -> new IllegalStateException("caseArgumentDocument is not present"));

        final String caseArgumentDescription =
            CaseDataMap
                .getCaseArgumentDescription()
                .orElse("");

        final List<IdValue<DocumentWithDescription>> caseArgumentEvidence =
            CaseDataMap
                .getCaseArgumentEvidence()
                .orElse(Collections.emptyList());

        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            CaseDataMap
                .getLegalRepresentativeDocuments()
                .orElse(Collections.emptyList());

        List<DocumentWithMetadata> caseArgumentDocuments = new ArrayList<>();

        caseArgumentDocuments.add(
            documentReceiver
                .receive(
                    caseArgumentDocument,
                    caseArgumentDescription,
                    DocumentTag.CASE_ARGUMENT
                )
        );

        caseArgumentDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    caseArgumentEvidence,
                    DocumentTag.CASE_ARGUMENT
                )
        );

        List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments =
            documentsAppender.append(
                legalRepresentativeDocuments,
                caseArgumentDocuments,
                DocumentTag.CASE_ARGUMENT
            );

        CaseDataMap.setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);

        CaseDataMap.setCaseArgumentAvailable(YesOrNo.Yes);

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
