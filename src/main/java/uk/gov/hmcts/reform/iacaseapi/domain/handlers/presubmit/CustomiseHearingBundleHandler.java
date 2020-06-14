package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.BundleRequestExecutor;


@Component
public class CustomiseHearingBundleHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final BundleRequestExecutor bundleRequestExecutor;
    private final Appender<DocumentWithMetadata> appender;
    private final DateProvider dateProvider;
    private final String emBundlerUrl;
    private final String emBundlerStitchUri;
    private final ObjectMapper objectMapper;

    public CustomiseHearingBundleHandler(
            @Value("${emBundler.url}") String emBundlerUrl,
            @Value("${emBundler.stitch.uri}") String emBundlerStitchUri,
            BundleRequestExecutor bundleRequestExecutor,
            Appender<DocumentWithMetadata> appender,
            DateProvider dateProvider,
            ObjectMapper objectMapper
    ) {
        this.emBundlerUrl = emBundlerUrl;
        this.emBundlerStitchUri = emBundlerStitchUri;
        this.bundleRequestExecutor = bundleRequestExecutor;
        this.appender = appender;
        this.dateProvider = dateProvider;
        this.objectMapper = objectMapper;

    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.CUSTOMISE_HEARING_BUNDLE;
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
        asylumCase.clear(AsylumCaseFieldDefinition.HMCTS);
        asylumCase.write(AsylumCaseFieldDefinition.HMCTS, "[userImage:hmcts.png]");

        asylumCase.clear(AsylumCaseFieldDefinition.CASE_BUNDLES);
        asylumCase.write(AsylumCaseFieldDefinition.BUNDLE_CONFIGURATION, "iac-hearing-bundle-config.yaml");
        asylumCase.write(AsylumCaseFieldDefinition.BUNDLE_FILE_NAME_PREFIX, getBundlePrefix(asylumCase));

        //deep copy the case
        AsylumCase asylumCaseCopy;
        try {
            asylumCaseCopy = objectMapper
                    .readValue(objectMapper.writeValueAsString(asylumCase), AsylumCase.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot make a deep copy of the case");
        }

        prepareDocuments(asylumCaseCopy, CUSTOM_HEARING_DOCUMENTS, HEARING_DOCUMENTS);
        prepareDocuments(asylumCaseCopy, CUSTOM_LEGAL_REP_DOCUMENTS, LEGAL_REPRESENTATIVE_DOCUMENTS);
        prepareDocuments(asylumCaseCopy, CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, ADDITIONAL_EVIDENCE_DOCUMENTS);
        prepareDocuments(asylumCaseCopy, CUSTOM_RESPONDENT_DOCUMENTS, RESPONDENT_DOCUMENTS);

        final PreSubmitCallbackResponse<AsylumCase> response = bundleRequestExecutor.post(
                new Callback<>(
                        new CaseDetails<>(
                                callback.getCaseDetails().getId(),
                                callback.getCaseDetails().getJurisdiction(),
                                callback.getCaseDetails().getState(),
                                asylumCaseCopy,
                                callback.getCaseDetails().getCreatedDate()
                        ),
                        callback.getCaseDetailsBefore(),
                        callback.getEvent()
                ),
                emBundlerUrl + emBundlerStitchUri);

        final AsylumCase responseData = response.getData();

        restoreFolders(asylumCase, asylumCaseCopy);

        Optional<List<IdValue<Bundle>>> maybeCaseBundles = responseData.read(AsylumCaseFieldDefinition.CASE_BUNDLES);
        asylumCase.write(AsylumCaseFieldDefinition.CASE_BUNDLES, maybeCaseBundles);

        final List<Bundle> caseBundles = maybeCaseBundles
                .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
                .stream()
                .map(IdValue::getValue)
                .collect(Collectors.toList());

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stitchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final String stitchStatus = caseBundles.get(0).getStitchStatus().orElse("");

        asylumCase.write(AsylumCaseFieldDefinition.STITCHING_STATUS, stitchStatus);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }


    private Optional<IdValue<DocumentWithMetadata>> isPresent(
            List<IdValue<DocumentWithMetadata>> legalDocuments,
            IdValue<DocumentWithDescription> documentWithDescription
    ) {

        IdValue<DocumentWithMetadata> documentWithMetadataIdValue = null;

        for (IdValue<DocumentWithMetadata> doc : legalDocuments) {
            Document legalDocument = doc.getValue().getDocument();
            Document document = documentWithDescription.getValue().getDocument().orElseThrow(() -> new IllegalStateException("Document cannot be null"));
            if (legalDocument.getDocumentBinaryUrl().equals(document.getDocumentBinaryUrl())) {
                documentWithMetadataIdValue = doc;
            }

        }

        return Optional.ofNullable(documentWithMetadataIdValue);
    }

    private void restoreFolders(
            AsylumCase asylumCase,
            AsylumCase asylumCaseBefore
    ) {
        getFieldDefinitions().forEach(field -> {
            Optional<List<IdValue<DocumentWithMetadata>>> currentIdValues = asylumCase.read(field);
            Optional<List<IdValue<DocumentWithMetadata>>> beforeIdValues = asylumCaseBefore.read(field);

            List<IdValue<DocumentWithMetadata>> beforeDocuments = new ArrayList<>();

            if (beforeIdValues.isPresent()) {
                beforeDocuments = getIdValuesBefore(asylumCaseBefore, field);
            }
            //filter any document missing from the current list of document
            List<IdValue<DocumentWithMetadata>> missingDocuments = beforeDocuments
                    .stream()
                    .filter(document -> !contains(currentIdValues.orElse(emptyList()), document))
                    .collect(Collectors.toList());

            List<IdValue<DocumentWithMetadata>> allDocuments = currentIdValues.orElse(emptyList());
            for (IdValue<DocumentWithMetadata> documentWithMetadata : missingDocuments) {
                allDocuments = appender.append(documentWithMetadata.getValue(), allDocuments);
            }

            asylumCase.clear(field);
            asylumCase.write(field, allDocuments);
        });
    }

    private boolean contains(
            List<IdValue<DocumentWithMetadata>> legalDocuments,
            IdValue<DocumentWithMetadata> documentWithMetadata
    ) {

        boolean found = false;

        for (IdValue<DocumentWithMetadata> doc : legalDocuments) {
            Document legalDocument = doc.getValue().getDocument();
            Document document = documentWithMetadata.getValue().getDocument();
            if (legalDocument.getDocumentBinaryUrl().equals(document.getDocumentBinaryUrl())) {
                found = true;
            }
        }

        return found;
    }

    private List<AsylumCaseFieldDefinition> getFieldDefinitions() {
        return Arrays.asList(
                HEARING_DOCUMENTS,
                LEGAL_REPRESENTATIVE_DOCUMENTS,
                ADDITIONAL_EVIDENCE_DOCUMENTS,
                RESPONDENT_DOCUMENTS
        );
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesBefore(
            AsylumCase asylumCaseBefore,
            AsylumCaseFieldDefinition fieldDefinition
    ) {

        if (asylumCaseBefore != null) {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesBeforeOptional = asylumCaseBefore
                    .read(fieldDefinition);
            if (idValuesBeforeOptional.isPresent()) {
                return idValuesBeforeOptional.get();
            }
        }

        return Collections.emptyList();
    }

    private String getBundlePrefix(AsylumCase asylumCase) {

        final String appealReferenceNumber =
                asylumCase
                        .read(AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER, String.class)
                        .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is not present"));

        final String appellantFamilyName =
                asylumCase
                        .read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class)
                        .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        return appealReferenceNumber.replace("/", " ")
                + "-" + appellantFamilyName;
    }


    private void prepareDocuments(AsylumCase asylumCase, AsylumCaseFieldDefinition sourceField, AsylumCaseFieldDefinition targetField) {
        if (!asylumCase.read(sourceField).isPresent()) {
            return;
        }
        List<IdValue<DocumentWithMetadata>> targetDocuments = getIdValuesBefore(asylumCase, targetField);

        Optional<List<IdValue<DocumentWithDescription>>> maybeDocuments =
                asylumCase.read(sourceField);

        List<IdValue<DocumentWithDescription>> documents =
                maybeDocuments.orElse(emptyList());

        List<IdValue<DocumentWithMetadata>> customDocuments = new ArrayList<>();

        if (documents != null && documents.size() > 0) {
            for (IdValue<DocumentWithDescription> documentWithDescription : documents) {
                //if the any document is missing the tag, add the appropriate tag to it.
                Optional<IdValue<DocumentWithMetadata>> maybeDocument = isPresent(targetDocuments, documentWithDescription);
                Document document = documentWithDescription.getValue().getDocument().orElseThrow(() -> new IllegalStateException("Document cannot be null"));

                DocumentWithMetadata newDocumentWithMetadata = null;
                if (maybeDocument.isPresent()) {
                    newDocumentWithMetadata = new DocumentWithMetadata(document,
                            documentWithDescription.getValue().getDescription().orElse(""),
                            dateProvider.now().toString(),
                            maybeDocument.get().getValue().getTag(),
                            "");

                } else {
                    if (sourceField == CUSTOM_HEARING_DOCUMENTS) {
                        newDocumentWithMetadata = new DocumentWithMetadata(document,
                                documentWithDescription.getValue().getDescription().orElse(""),
                                dateProvider.now().toString(),
                                DocumentTag.HEARING_NOTICE,
                                "");

                    } else if (sourceField == CUSTOM_LEGAL_REP_DOCUMENTS) {
                        newDocumentWithMetadata = new DocumentWithMetadata(document,
                                documentWithDescription.getValue().getDescription().orElse(""),
                                dateProvider.now().toString(),
                                DocumentTag.CASE_ARGUMENT,
                                "");
                    } else if (sourceField == CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS) {
                        newDocumentWithMetadata = new DocumentWithMetadata(document,
                                documentWithDescription.getValue().getDescription().orElse(""),
                                dateProvider.now().toString(),
                                DocumentTag.ADDITIONAL_EVIDENCE,
                                "");
                    } else if (sourceField == CUSTOM_RESPONDENT_DOCUMENTS) {
                        newDocumentWithMetadata = new DocumentWithMetadata(document,
                                documentWithDescription.getValue().getDescription().orElse(""),
                                dateProvider.now().toString(),
                                DocumentTag.RESPONDENT_EVIDENCE,
                                "");
                    }
                }
                customDocuments = appender.append(newDocumentWithMetadata, customDocuments);
            }
        }
        asylumCase.clear(targetField);
        asylumCase.write(targetField, customDocuments);
    }
}
