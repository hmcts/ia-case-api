package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.*;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;


@Component
public class CustomiseHearingBundlePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<DocumentWithDescription> appender;

    public CustomiseHearingBundlePreparer(Appender<DocumentWithDescription> appender) {
        this.appender = appender;
    }



    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.CUSTOMISE_HEARING_BUNDLE;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        prepareDocuments(asylumCase, HEARING_DOCUMENTS, CUSTOM_HEARING_DOCUMENTS);
        prepareDocuments(asylumCase, LEGAL_REPRESENTATIVE_DOCUMENTS, CUSTOM_LEGAL_REP_DOCUMENTS);
        prepareDocuments(asylumCase, ADDITIONAL_EVIDENCE_DOCUMENTS, CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS);
        prepareDocuments(asylumCase, RESPONDENT_DOCUMENTS, CUSTOM_RESPONDENT_DOCUMENTS);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public void prepareDocuments(AsylumCase asylumCase, AsylumCaseFieldDefinition sourceField, AsylumCaseFieldDefinition targetField) {
        if (!asylumCase.read(sourceField).isPresent()) {
            return;
        }

        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments =
                asylumCase.read(sourceField);

        List<IdValue<DocumentWithMetadata>> documents =
                maybeDocuments.orElse(emptyList());

        List<IdValue<DocumentWithDescription>> customLegalRepresentativeDocuments = new ArrayList<>();

        for (IdValue<DocumentWithMetadata> documentWithMetadata : documents) {
            DocumentWithDescription newDocumentWithDescription =
                    new DocumentWithDescription(documentWithMetadata.getValue().getDocument(),
                            documentWithMetadata.getValue().getDescription());

            if (sourceField == LEGAL_REPRESENTATIVE_DOCUMENTS) {
                if (documentWithMetadata.getValue().getTag() == DocumentTag.APPEAL_SUBMISSION
                        || documentWithMetadata.getValue().getTag() == DocumentTag.CASE_ARGUMENT) {
                    customLegalRepresentativeDocuments = appender.append(newDocumentWithDescription, customLegalRepresentativeDocuments);
                }
            } else {
                customLegalRepresentativeDocuments = appender.append(newDocumentWithDescription, customLegalRepresentativeDocuments);
            }
        }

        asylumCase.clear(targetField);
        asylumCase.write(targetField, customLegalRepresentativeDocuments);
    }
}
