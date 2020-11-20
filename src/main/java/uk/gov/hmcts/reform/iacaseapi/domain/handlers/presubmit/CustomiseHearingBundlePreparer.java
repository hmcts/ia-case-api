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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@Component
public class CustomiseHearingBundlePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<DocumentWithDescription> documentWithDescriptionAppender;
    private final FeatureToggler featureToggler;

    public CustomiseHearingBundlePreparer(Appender<DocumentWithDescription> documentWithDescriptionAppender,
                                          FeatureToggler featureToggler) {
        this.documentWithDescriptionAppender = documentWithDescriptionAppender;
        this.featureToggler = featureToggler;
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

        boolean isReheardCase = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
                               && featureToggler.getValue("reheard-feature", false);

        prepareCustomDocuments(asylumCase, isReheardCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public void prepareCustomDocuments(AsylumCase asylumCase, boolean isCaseReheard) {

        getMappingFields(isCaseReheard).forEach((sourceField,targetField)  ->
            populateCustomCollections(asylumCase, sourceField, targetField)
        );

        // Map does not accept duplicate keys, so need to process this separately
        if (isCaseReheard) {
            populateCustomCollections(asylumCase,ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        }
    }

    private void populateCustomCollections(AsylumCase asylumCase, AsylumCaseFieldDefinition sourceField, AsylumCaseFieldDefinition targetField) {
        if (!asylumCase.read(sourceField).isPresent()) {
            return;
        }

        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments =
            asylumCase.read(sourceField);

        List<IdValue<DocumentWithMetadata>> documents =
            maybeDocuments.orElse(emptyList());

        List<IdValue<DocumentWithDescription>> customDocuments = new ArrayList<>();

        for (IdValue<DocumentWithMetadata> documentWithMetadata : documents) {
            DocumentWithDescription newDocumentWithDescription =
                new DocumentWithDescription(documentWithMetadata.getValue().getDocument(),
                    documentWithMetadata.getValue().getDescription());

            if (sourceField == LEGAL_REPRESENTATIVE_DOCUMENTS) {
                if (documentWithMetadata.getValue().getTag() == DocumentTag.APPEAL_SUBMISSION
                    || documentWithMetadata.getValue().getTag() == DocumentTag.CASE_ARGUMENT) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else if (targetField == CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS) {
                if ("The appellant".equals(documentWithMetadata.getValue().getSuppliedBy())) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else if (targetField == CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS) {
                if ("The respondent".equals(documentWithMetadata.getValue().getSuppliedBy())) {
                    customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
                }
            } else {
                customDocuments = documentWithDescriptionAppender.append(newDocumentWithDescription, customDocuments);
            }
        }

        asylumCase.clear(targetField);
        asylumCase.write(targetField, customDocuments);
    }

    private Map<AsylumCaseFieldDefinition,AsylumCaseFieldDefinition> getMappingFields(boolean isReheardCase) {

        if (isReheardCase) {
            return  Map.of(
                APP_ADDITIONAL_EVIDENCE_DOCS, CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS,
                RESP_ADDITIONAL_EVIDENCE_DOCS, CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS,
                FTPA_APPELLANT_DOCUMENTS, CUSTOM_FTPA_APPELLANT_DOCS,
                FTPA_RESPONDENT_DOCUMENTS, CUSTOM_FTPA_RESPONDENT_DOCS,
                FINAL_DECISION_AND_REASONS_DOCUMENTS, CUSTOM_FINAL_DECISION_AND_REASONS_DOCS,
                REHEARD_HEARING_DOCUMENTS, CUSTOM_REHEARD_HEARING_DOCS,
                ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);

        } else {
            return  Map.of(
                HEARING_DOCUMENTS, CUSTOM_HEARING_DOCUMENTS,
                LEGAL_REPRESENTATIVE_DOCUMENTS, CUSTOM_LEGAL_REP_DOCUMENTS,
                ADDITIONAL_EVIDENCE_DOCUMENTS, CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS,
                RESPONDENT_DOCUMENTS, CUSTOM_RESPONDENT_DOCUMENTS);
        }
    }

}
