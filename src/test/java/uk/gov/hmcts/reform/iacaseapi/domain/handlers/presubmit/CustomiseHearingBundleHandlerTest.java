package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDENDUM_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_BUNDLES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_FINAL_DECISION_AND_REASONS_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_FTPA_APPELLANT_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_FTPA_RESPONDENT_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_LEGAL_REP_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_REHEARD_HEARING_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HMCTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARD_HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_ADDENDUM_EVIDENCE_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STITCHING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.SystemDateProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.BundleRequestExecutor;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CustomiseHearingBundleHandlerTest {

    @Mock
    private BundleRequestExecutor bundleRequestExecutor;
    @Mock
    private Appender<DocumentWithMetadata> appender;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseCopy;

    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private String emBundlerUrl = "bundleurl";
    private String emBundlerStitchUri = "stitchingUri";
    private String appealReference = "PA/50002/2020";
    private String appellantFamilyName = "bond";
    private String coverPageLogo = "[userImage:hmcts.png]";
    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    private CustomiseHearingBundleHandler customiseHearingBundleHandler;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        customiseHearingBundleHandler =
            new CustomiseHearingBundleHandler(
                emBundlerUrl,
                emBundlerStitchUri,
                bundleRequestExecutor,
                appender,
                dateProvider,
                objectMapper,
                featureToggler
            );

        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));
        when(bundleRequestExecutor.post(any(Callback.class), eq(emBundlerUrl + emBundlerStitchUri)))
            .thenReturn(callbackResponse);

        when(objectMapper.writeValueAsString(any(AsylumCase.class))).thenReturn("Test");
        when(objectMapper.readValue("Test", AsylumCase.class)).thenReturn(asylumCaseCopy);

        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(appealReference));

        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(appellantFamilyName));

        when(callbackResponse.getData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle =
            new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.empty(),
                YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));

        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @Test
    public void should_successfully_handle_the_callback() throws JsonProcessingException {

        IdValue<DocumentWithDescription> legalRepDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> hearingDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> additionalEvidenceDoc = new IdValue<>("1", createDocumentWithDescription());


        when(asylumCaseCopy.read(CUSTOM_HEARING_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(hearingDoc)));
        when(asylumCaseCopy.read(CUSTOM_LEGAL_REP_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(legalRepDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentDoc)));
        when(asylumCaseCopy.read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(additionalEvidenceDoc)));

        IdValue<DocumentWithMetadata> legalRepDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));
        IdValue<DocumentWithMetadata> respondentDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.APPEAL_RESPONSE, "test"));
        IdValue<DocumentWithMetadata> hearingDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test"));
        IdValue<DocumentWithMetadata> additionalEvidenceDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));

        final List<IdValue<DocumentWithMetadata>> hearingDocuments = Lists.newArrayList(hearingDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            Lists.newArrayList(legalRepDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments =
            Lists.newArrayList(additionalEvidenceDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> respondentDocuments = Lists.newArrayList(respondentDocWithMetadata);

        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(hearingDocWithMetadata)));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(legalRepDocWithMetadata)));
        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentDocWithMetadata)));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(additionalEvidenceDocWithMetadata)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCaseCopy, times(2)).read(CUSTOM_HEARING_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESPONDENT_DOCUMENTS);

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, hearingDocuments);
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, legalRepresentativeDocuments);
        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS, additionalEvidenceDocuments);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, respondentDocuments);

        verify(asylumCase).clear(AsylumCaseFieldDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);

        verify(asylumCase).clear(AsylumCaseFieldDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseFieldDefinition.BUNDLE_CONFIGURATION, "iac-hearing-bundle-config.yaml");
        verify(asylumCase)
            .write(AsylumCaseFieldDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));

    }

    @Test
    public void should_successfully_handle_Reheard_the_callback() throws JsonProcessingException {
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IdValue<DocumentWithDescription> appellantAdditionalEvidenceDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentAdditionalEvidenceDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> ftpaAppellantDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> ftpaRespondentDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> finalDecisionAndReasonsDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> hearingDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> appellantAddendumEvidenceDoc =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentAddendumEvidenceDoc =
            new IdValue<>("1", createDocumentWithDescription());


        when(asylumCaseCopy.read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FTPA_APPELLANT_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaAppellantDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FTPA_RESPONDENT_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaRespondentDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(finalDecisionAndReasonsDocuments)));
        when(asylumCaseCopy.read(CUSTOM_REHEARD_HEARING_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(hearingDocuments)));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS)).thenReturn(Optional
            .of(Lists.newArrayList(new IdValue<>("2", createDocumentWithDescription()), appellantAddendumEvidenceDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAddendumEvidenceDoc)));

        IdValue<DocumentWithMetadata> appellantAdditionalEvidenceDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> respondentAdditionalEvidenceDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "The respondent"));
        IdValue<DocumentWithMetadata> ftpaAppellantDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test"));
        IdValue<DocumentWithMetadata> ftpaRespondentDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test"));
        IdValue<DocumentWithMetadata> finalDecisionsAndReasonsDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_DECISION_AND_REASONS, "test"));
        IdValue<DocumentWithMetadata> reheardHearingDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test"));
        IdValue<DocumentWithMetadata> appellantAddendumEvidenceDocs =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> respondentAddendumEvidenceDocs =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent"));


        final List<IdValue<DocumentWithMetadata>> appellantAdditionalEvidenceDocs =
            Lists.newArrayList(appellantAdditionalEvidenceDoc);
        final List<IdValue<DocumentWithMetadata>> respondentAdditionalEvidenceDocs =
            Lists.newArrayList(respondentAdditionalEvidenceDoc);
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocs = Lists.newArrayList(ftpaAppellantDoc);
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocs = Lists.newArrayList(ftpaRespondentDoc);
        final List<IdValue<DocumentWithMetadata>> finalDecisionsAndReasonsDocs =
            Lists.newArrayList(finalDecisionsAndReasonsDoc);
        final List<IdValue<DocumentWithMetadata>> reheardHearingDocs = Lists.newArrayList(reheardHearingDoc);
        final List<IdValue<DocumentWithMetadata>> appellantAddendumEvidenceList =
            Lists.newArrayList(appellantAddendumEvidenceDocs);
        final List<IdValue<DocumentWithMetadata>> respondentAddendumEvidenceList =
            Lists.newArrayList(respondentAddendumEvidenceDocs);
        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = asList(
            new IdValue("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
            new IdValue("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        when(appender.append(any(DocumentWithMetadata.class), anyList()))
            .thenReturn(addendumEvidenceDocumentList);

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocs)));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocs)));
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(ftpaAppellantDocs)));
        when(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaRespondentDocs)));
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(finalDecisionsAndReasonsDocs)));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCase.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCaseCopy.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAddendumEvidenceList)));
        when(asylumCaseCopy.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAddendumEvidenceList)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class), Optional.of(YesOrNo.YES));
        verify(asylumCaseCopy, times(4)).read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(4)).read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FTPA_APPELLANT_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FTPA_RESPONDENT_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_REHEARD_HEARING_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);

        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS, appellantAdditionalEvidenceDocs);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, respondentAdditionalEvidenceDocs);
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_DOCUMENTS, ftpaAppellantDocs);
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_DOCUMENTS, ftpaRespondentDocs);
        verify(asylumCase, times(1)).write(FINAL_DECISION_AND_REASONS_DOCUMENTS, finalDecisionsAndReasonsDocs);
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS, reheardHearingDocs);
        verify(asylumCase, times(1)).write(ADDENDUM_EVIDENCE_DOCUMENTS, addendumEvidenceDocumentList);

        verify(asylumCase).clear(AsylumCaseFieldDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.CASE_BUNDLES);
        verify(asylumCase)
            .write(AsylumCaseFieldDefinition.BUNDLE_CONFIGURATION, "iac-reheard-hearing-bundle-config.yaml");
        verify(asylumCase)
            .write(AsylumCaseFieldDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));
    }

    @Test
    public void should_throw_when_appeal_reference_is_not_present() {

        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_asylumcase_can_not_copied() throws JsonProcessingException {

        when(objectMapper.readValue("Test", AsylumCase.class))
            .thenThrow(new IllegalStateException("Cannot make a deep copy of the case"));

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot make a deep copy of the case")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_appellant_family_name_is_not_present() {

        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_case_bundle_is_not_present() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_case_bundle_is_empty() {

        caseBundles.clear();

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("case bundles size is not 1 and is : 0")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = customiseHearingBundleHandler.canHandle(callbackStage, callback);

                if (event == Event.CUSTOMISE_HEARING_BUNDLE
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> customiseHearingBundleHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private DocumentWithDescription createDocumentWithDescription() {
        return
            new DocumentWithDescription(new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20)), "test");
    }

    private Document createDocument() {
        return
            new Document("some-url",
                "some-binary-url",
                "some-filename");
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag, String suppliedBy) {

        return
            new DocumentWithMetadata(createDocument(),
                "some-description",
                new SystemDateProvider().now().toString(), documentTag, suppliedBy);

    }

}
