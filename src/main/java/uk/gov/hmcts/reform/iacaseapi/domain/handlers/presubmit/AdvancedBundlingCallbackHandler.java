package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.BundleRequestExecutor;


@Component
public class AdvancedBundlingCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String SUPPLIED_BY_RESPONDENT = "The respondent";
    private static final String SUPPLIED_BY_APPELLANT = "The appellant";

    private final BundleRequestExecutor bundleRequestExecutor;
    private final String emBundlerUrl;
    private final String emBundlerStitchUri;



    public AdvancedBundlingCallbackHandler(
        @Value("${emBundler.url}") String emBundlerUrl,
        @Value("${emBundler.stitch.uri}") String emBundlerStitchUri,
        BundleRequestExecutor bundleRequestExecutor) {
        this.emBundlerUrl = emBundlerUrl;
        this.emBundlerStitchUri = emBundlerStitchUri;
        this.bundleRequestExecutor = bundleRequestExecutor;

    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.GENERATE_HEARING_BUNDLE;
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
        asylumCase.write(AsylumCaseFieldDefinition.HMCTS,"[userImage:hmcts.png]");
        asylumCase.clear(AsylumCaseFieldDefinition.CASE_BUNDLES);

        Optional<YesOrNo> maybeCaseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS,YesOrNo.class);

        if (maybeCaseFlagSetAsideReheardExists.isPresent()
                && maybeCaseFlagSetAsideReheardExists.get() == YesOrNo.YES) {

            asylumCase.write(APPELLANT_ADDENDUM_EVIDENCE_DOCS,getIdValues(asylumCase,ADDENDUM_EVIDENCE_DOCUMENTS, SUPPLIED_BY_APPELLANT,DocumentTag.ADDENDUM_EVIDENCE));
            asylumCase.write(RESPONDENT_ADDENDUM_EVIDENCE_DOCS,getIdValues(asylumCase,ADDENDUM_EVIDENCE_DOCUMENTS, SUPPLIED_BY_RESPONDENT,DocumentTag.ADDENDUM_EVIDENCE));

            asylumCase.write(APP_ADDITIONAL_EVIDENCE_DOCS,getIdValues(asylumCase, ADDITIONAL_EVIDENCE_DOCUMENTS, SUPPLIED_BY_APPELLANT,DocumentTag.ADDITIONAL_EVIDENCE));
            asylumCase.write(RESP_ADDITIONAL_EVIDENCE_DOCS,getIdValues(asylumCase, RESPONDENT_DOCUMENTS, SUPPLIED_BY_RESPONDENT,DocumentTag.ADDITIONAL_EVIDENCE));

            asylumCase.write(AsylumCaseFieldDefinition.BUNDLE_CONFIGURATION, "iac-reheard-hearing-bundle-config.yaml");
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.BUNDLE_CONFIGURATION, "iac-hearing-bundle-config.yaml");
        }

        asylumCase.write(AsylumCaseFieldDefinition.BUNDLE_FILE_NAME_PREFIX, getBundlePrefix(asylumCase));

        final PreSubmitCallbackResponse<AsylumCase> response = bundleRequestExecutor.post(callback, emBundlerUrl + emBundlerStitchUri);

        final AsylumCase responseData = response.getData();
        Optional<List<IdValue<Bundle>>> maybeCaseBundles  = responseData.read(AsylumCaseFieldDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList());

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stictchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final String stitchStatus = caseBundles.get(0).getStitchStatus().orElse("");

        responseData.write(AsylumCaseFieldDefinition.STITCHING_STATUS, stitchStatus);

        return new PreSubmitCallbackResponse<>(responseData);
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

    private List<IdValue<DocumentWithMetadata>> getIdValues(
        AsylumCase asylumCase,
        AsylumCaseFieldDefinition fieldDefinition,String suppliedBy, DocumentTag tag
    ) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeIdValues = asylumCase
            .read(fieldDefinition);

        List<IdValue<DocumentWithMetadata>> documents =
            maybeIdValues.orElse(Collections.emptyList());
        if (fieldDefinition == ADDENDUM_EVIDENCE_DOCUMENTS) {
            return documents.stream()
                .filter(document -> document.getValue().getSuppliedBy().equals(suppliedBy))
                .filter(document -> document.getValue().getTag() == tag)
                .collect(Collectors.toList());
        } else {
            return documents.stream()
                .filter(document -> document.getValue().getTag() == tag)
                .collect(Collectors.toList());
        }


    }

}
