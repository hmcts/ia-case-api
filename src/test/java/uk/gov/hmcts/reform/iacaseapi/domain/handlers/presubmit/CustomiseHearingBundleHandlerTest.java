package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.BundleRequestExecutor;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CustomiseHearingBundleHandlerTest {

    @Mock private BundleRequestExecutor bundleRequestExecutor;
    @Mock private Appender<DocumentWithMetadata> appender;
    @Mock private DateProvider dateProvider;
    @Mock private ObjectMapper objectMapper;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseCopy;

    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    @Mock private List<IdValue<DocumentWithDescription>> customHearingDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> hearingDocuments;

    @Mock private List<IdValue<DocumentWithDescription>> customLegalRepresentativeDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments;

    @Mock private List<IdValue<DocumentWithDescription>> customAdditionalEvidenceDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments;

    @Mock private List<IdValue<DocumentWithDescription>> customRespondentDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> respondentDocuments;


    String emBundlerUrl = "bundleurl";
    String emBundlerStitchUri = "stitchingUri";
    String appealReference = "PA/50002/2020";
    String appellantFamilyName = "bond";
    String coverPageLogo = "[userImage:hmcts.png]";
    List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    CustomiseHearingBundleHandler customiseHearingBundleHandler;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        customiseHearingBundleHandler =
                new CustomiseHearingBundleHandler(
                        emBundlerUrl,
                        emBundlerStitchUri,
                        bundleRequestExecutor,
                        appender,
                        dateProvider,
                        objectMapper
                );

        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));
        when(bundleRequestExecutor.post(any(Callback.class),eq(emBundlerUrl + emBundlerStitchUri))).thenReturn(callbackResponse);

        when(objectMapper.writeValueAsString(any(AsylumCase.class))).thenReturn("Test");
        when(objectMapper.readValue("Test",AsylumCase.class)).thenReturn(asylumCaseCopy);

        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReference));

        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(callbackResponse.getData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.empty(), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));
    }

    @Test
    void should_successfully_handle_the_callback() throws JsonProcessingException {
        when(asylumCase.read(HEARING_DOCUMENTS))
                .thenReturn(Optional.of(hearingDocuments));

        when(asylumCase.read(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS))
                .thenReturn(Optional.of(legalRepresentativeDocuments));

        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
                .thenReturn(Optional.of(additionalEvidenceDocuments));

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
                .thenReturn(Optional.of(respondentDocuments));

        when(asylumCaseCopy.read(CUSTOM_HEARING_DOCUMENTS))
                .thenReturn(Optional.of(customHearingDocuments));
        when(asylumCaseCopy.read(CUSTOM_LEGAL_REP_DOCUMENTS))
                .thenReturn(Optional.of(customLegalRepresentativeDocuments));
        when(asylumCaseCopy.read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS))
                .thenReturn(Optional.of(customAdditionalEvidenceDocuments));
        when(asylumCaseCopy.read(CUSTOM_RESPONDENT_DOCUMENTS))
                .thenReturn(Optional.of(customRespondentDocuments));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCaseCopy,times(2)).read(CUSTOM_HEARING_DOCUMENTS);
        verify(asylumCaseCopy,times(2)).read(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCaseCopy,times(2)).read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCaseCopy,times(2)).read(CUSTOM_RESPONDENT_DOCUMENTS);

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS,hearingDocuments);
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS,legalRepresentativeDocuments);
        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS,additionalEvidenceDocuments);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS,respondentDocuments);


        verify(asylumCase).clear(AsylumCaseFieldDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);

        verify(asylumCase).clear(AsylumCaseFieldDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseFieldDefinition.BUNDLE_CONFIGURATION, "iac-hearing-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseFieldDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));

    }


    @Test
    void should_throw_when_appeal_reference_is_not_present() {

        when(asylumCase.read(AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("appealReferenceNumber is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_asylumcase_can_not_copied() throws JsonProcessingException {

        when(objectMapper.readValue("Test", AsylumCase.class)).thenThrow(new IllegalStateException("Cannot make a deep copy of the case"));

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot make a deep copy of the case")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_appellant_family_name_is_not_present() {

        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("appellantFamilyName is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_not_present() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("caseBundle is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_empty() {

        caseBundles.clear();

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("case bundles size is not 1 and is : 0")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

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
    void should_not_allow_null_arguments() {

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
}
