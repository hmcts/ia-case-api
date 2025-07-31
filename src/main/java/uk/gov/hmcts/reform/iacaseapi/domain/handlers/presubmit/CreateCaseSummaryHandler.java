package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReheardHearingDocuments;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class CreateCaseSummaryHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final FeatureToggler featureToggler;

    public CreateCaseSummaryHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        FeatureToggler featureToggler
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CREATE_CASE_SUMMARY;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final Document caseSummaryDocument =
            asylumCase
                .read(CASE_SUMMARY_DOCUMENT, Document.class)
                .orElseThrow(() -> new IllegalStateException("caseSummaryDocument is not present"));

        final String caseSummaryDescription =
            asylumCase
                .read(CASE_SUMMARY_DESCRIPTION, String.class)
                .orElse("");

        DocumentWithMetadata caseSummaryDocumentWithMetadata =
            documentReceiver.receive(
                caseSummaryDocument,
                caseSummaryDescription,
                DocumentTag.CASE_SUMMARY
            );

        Optional<YesOrNo> caseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS);
        boolean remittedFeatureFlag = featureToggler.getValue("dlrm-remitted-feature-flag", false);

        List<IdValue<DocumentWithMetadata>> allHearingDocuments =
            documentsAppender.append(
                fetchHearingDocuments(asylumCase, caseFlagSetAsideReheardExists, remittedFeatureFlag),
                singletonList(caseSummaryDocumentWithMetadata),
                DocumentTag.CASE_SUMMARY
            );

        if (caseFlagSetAsideReheardExists.isPresent() && caseFlagSetAsideReheardExists.get() == YesOrNo.YES) {
            if (remittedFeatureFlag) {
                Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
                        asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
                List<IdValue<ReheardHearingDocuments>> existingReheardDocuments = maybeExistingReheardDocuments.orElse(emptyList());
                if (!existingReheardDocuments.isEmpty()) {
                    existingReheardDocuments.get(0).getValue().setReheardHearingDocs(allHearingDocuments);
                } else {
                    Appender<ReheardHearingDocuments> documentsCollectionAppender =
                        new Appender<>();
                    ReheardHearingDocuments reheardHearingDocuments = new ReheardHearingDocuments(allHearingDocuments);
                    existingReheardDocuments = documentsCollectionAppender.append(reheardHearingDocuments, existingReheardDocuments);
                }
                asylumCase.write(REHEARD_HEARING_DOCUMENTS_COLLECTION, existingReheardDocuments);
            } else {
                asylumCase.write(REHEARD_HEARING_DOCUMENTS, allHearingDocuments);
            }
        } else {
            asylumCase.write(HEARING_DOCUMENTS, allHearingDocuments);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<IdValue<DocumentWithMetadata>> fetchHearingDocuments(AsylumCase asylumCase,
                                                                      Optional<YesOrNo> caseFlagSetAsideReheardExists,
                                                                      boolean remittedFlag) {

        boolean isSetAsideReheard = caseFlagSetAsideReheardExists.map(flag -> flag.equals(YesOrNo.YES)).orElse(false);

        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments = isSetAsideReheard
                        ? asylumCase.read(REHEARD_HEARING_DOCUMENTS)
                        : asylumCase.read(HEARING_DOCUMENTS);

        if (isSetAsideReheard && remittedFlag) {
            Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
                    asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
            List<IdValue<ReheardHearingDocuments>> existingReheardDocuments = maybeExistingReheardDocuments.orElse(emptyList());

            return (!existingReheardDocuments.isEmpty())
                    ? existingReheardDocuments.get(0).getValue().getReheardHearingDocs()
                    : emptyList();
        }
        return maybeHearingDocuments.orElse(emptyList());
    }
}
