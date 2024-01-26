package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_APPLICATION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_GROUNDS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_APPLICATION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_GROUNDS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FtpaDisplayService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaMigrateLegacyDataHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Appender<FtpaApplications> ftpaAppender;
    @Mock
    private FtpaDisplayService ftpaDisplayService;
    @Mock
    private List<IdValue<DocumentWithDescription>> groundsOfApplicationDocuments;
    @Mock
    private List<IdValue<DocumentWithDescription>> evidenceDocuments;
    @Mock
    private List<IdValue<DocumentWithDescription>> outOfTimeDocuments;
    @Captor
    private ArgumentCaptor<List<IdValue<FtpaApplications>>> existingFtpasCaptor;
    @Captor private ArgumentCaptor<FtpaApplications> newFtpaCaptor;
    @Captor
    private ArgumentCaptor<List<IdValue<Direction>>> asylumValueCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private FtpaMigrateLegacyDataHandler ftpaMigrateLegacyDataHandler;
    private final String ftpaApplicationDate = "2024-02-01";
    private final String ftpaOotExplanation = "Some out of time explanation";

    @BeforeEach
    public void setUp() {
        ftpaMigrateLegacyDataHandler =
                new FtpaMigrateLegacyDataHandler(featureToggler, ftpaAppender, ftpaDisplayService);
        when(featureToggler.getValue("dlrm-setaside-feature-flag", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    void should_write_ftpa_list_if_list_exist_has_decision(String applicantType) {

        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.empty());
        if (applicantType.equals("appellant")) {
            buildAppellantFtpaStarted(asylumCaseBefore);
        } else {
            buildRespondentFtpaStarted(asylumCaseBefore);
        }

        FtpaApplications newFtpaApplication =
                FtpaApplications.builder()
                        .ftpaApplicant(applicantType)
                        .ftpaDecisionDate("2024-02-02")
                        .build();

        List<IdValue<FtpaApplications>> ftpaApplications = Lists.newArrayList(new IdValue<>("1",
                newFtpaApplication));

        when(ftpaAppender.append(any(FtpaApplications.class), anyList())).thenReturn(ftpaApplications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(ftpaDisplayService, times(1)).mapFtpaDecision(any(AsylumCase.class), anyString(), any(FtpaApplications.class));
        verify(asylumCase, times(1)).write(FTPA_LIST, ftpaApplications);
        assertEquals("2024-02-01", newFtpaApplication.getFtpaApplicationDate());
        assertEquals(groundsOfApplicationDocuments, newFtpaApplication.getFtpaGroundsDocuments());
        assertEquals(evidenceDocuments, newFtpaApplication.getFtpaEvidenceDocuments());
        assertEquals(outOfTimeDocuments, newFtpaApplication.getFtpaOutOfTimeDocuments());
        assertEquals(ftpaOotExplanation, newFtpaApplication.getFtpaOutOfTimeExplanation());
    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    void should_write_ftpa_list_if_list_exist_has_no_decision(String applicantType) {

        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.empty());
        if (applicantType.equals("appellant")) {
            buildAppellantFtpaStarted(asylumCaseBefore);
            when(asylumCaseBefore.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)).thenReturn(Optional.empty());
        } else {
            buildRespondentFtpaStarted(asylumCaseBefore);
            when(asylumCaseBefore.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)).thenReturn(Optional.empty());
        }

        FtpaApplications newFtpaApplication =
                FtpaApplications.builder()
                        .ftpaApplicant(applicantType)
                        .build();

        List<IdValue<FtpaApplications>> ftpaApplications = Lists.newArrayList(new IdValue<>("1",
                newFtpaApplication));

        when(ftpaAppender.append(any(FtpaApplications.class), anyList())).thenReturn(ftpaApplications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(ftpaDisplayService, times(0)).mapFtpaDecision(any(AsylumCase.class), anyString(), any(FtpaApplications.class));
        verify(ftpaAppender, times(1))
                .append(newFtpaCaptor.capture(), existingFtpasCaptor.capture());
        FtpaApplications capturedFtpa = newFtpaCaptor.getValue();

        verify(asylumCase, times(1)).write(FTPA_LIST, ftpaApplications);
        assertThat(capturedFtpa.getFtpaApplicationDate()).isEqualTo(ftpaApplicationDate);
        assertThat(capturedFtpa.getFtpaGroundsDocuments()).isEqualTo(groundsOfApplicationDocuments);
        assertThat(capturedFtpa.getFtpaEvidenceDocuments()).isEqualTo(evidenceDocuments);
        assertThat(capturedFtpa.getFtpaOutOfTimeExplanation()).isEqualTo(ftpaOotExplanation);
        assertThat(capturedFtpa.getFtpaOutOfTimeDocuments()).isEqualTo(outOfTimeDocuments);
    }

    @Test
    void should_append_2_ftpa_data_appellant_and_respondent() {
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.empty());
        buildAppellantFtpaStarted(asylumCaseBefore);
        when(asylumCaseBefore.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)).thenReturn(Optional.empty());
        buildRespondentFtpaStarted(asylumCaseBefore);
        when(asylumCaseBefore.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(ftpaDisplayService, times(0)).mapFtpaDecision(any(AsylumCase.class), anyString(), any(FtpaApplications.class));
        verify(ftpaAppender, times(2)).append(newFtpaCaptor.capture(), existingFtpasCaptor.capture());
        assertEquals(2, existingFtpasCaptor.getAllValues().size());
        verify(asylumCase, times(1)).write(asylumExtractorCaptor.capture(), asylumValueCaptor.capture());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "APPLY_FOR_FTPA_APPELLANT", "APPLY_FOR_FTPA_RESPONDENT", "RESIDENT_JUDGE_FTPA_DECISION"
    })
    void should_not_write_ftpa_list_if_list_exist(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        List<IdValue<FtpaApplications>> ftpaApplications = Lists.newArrayList(new IdValue<>("1",
                FtpaApplications.builder()
                        .ftpaApplicant("respondent")
                        .build()));
        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.of(ftpaApplications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(0)).write(FTPA_LIST, ftpaApplications);
    }

    @Test
    void should_be_handled_earliest() {
        assertEquals(DispatchPriority.EARLIEST, ftpaMigrateLegacyDataHandler.getDispatchPriority());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "APPLY_FOR_FTPA_APPELLANT", "APPLY_FOR_FTPA_RESPONDENT", "RESIDENT_JUDGE_FTPA_DECISION"
    })
    void cannot_handle_if_feature_flag_disabled(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("dlrm-setaside-feature-flag", false)).thenReturn(false);

        boolean canHandle = ftpaMigrateLegacyDataHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertFalse(canHandle);
    }

    @ParameterizedTest
    @ValueSource(strings = {"appellant", "respondent"})
    void should_throw_if_application_date_missing(String applicantType) {

        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(FTPA_LIST)).thenReturn(Optional.empty());
        if (applicantType.equals("appellant")) {
            when(asylumCaseBefore.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        } else {
            when(asylumCaseBefore.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        }

        assertThatThrownBy(() -> ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("ftpaApplicationDate is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = ftpaMigrateLegacyDataHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                        Event.APPLY_FOR_FTPA_APPELLANT,
                        Event.APPLY_FOR_FTPA_RESPONDENT,
                        Event.RESIDENT_JUDGE_FTPA_DECISION)
                        .contains(event)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
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

        assertThatThrownBy(() -> ftpaMigrateLegacyDataHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaMigrateLegacyDataHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "APPLY_FOR_FTPA_APPELLANT", "APPLY_FOR_FTPA_RESPONDENT", "RESIDENT_JUDGE_FTPA_DECISION"
    })
    void should_throw_exception_if_previous_case_data_not_found(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ftpaMigrateLegacyDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("cannot find previous details")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    private void buildRespondentFtpaStarted(AsylumCase asylumCaseBeforeData) {
        when(asylumCaseBeforeData.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)).thenReturn(Optional.of("granted"));
        when(asylumCaseBeforeData.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        when(asylumCaseBeforeData.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(evidenceDocuments));
        when(asylumCaseBeforeData.read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCaseBeforeData.read(FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION, String.class))
                .thenReturn(Optional.of(ftpaOotExplanation));
        when(asylumCaseBeforeData.read(FTPA_RESPONDENT_APPLICATION_DATE, String.class)).thenReturn(Optional.of(ftpaApplicationDate));
    }

    private void buildAppellantFtpaStarted(AsylumCase asylumCaseBeforeData) {
        when(asylumCaseBeforeData.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)).thenReturn(Optional.of("granted"));
        when(asylumCaseBeforeData.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(groundsOfApplicationDocuments));
        when(asylumCaseBeforeData.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(evidenceDocuments));
        when(asylumCaseBeforeData.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS)).thenReturn(Optional.of(outOfTimeDocuments));
        when(asylumCaseBeforeData.read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class))
                .thenReturn(Optional.of(ftpaOotExplanation));
        when(asylumCaseBeforeData.read(FTPA_APPELLANT_APPLICATION_DATE, String.class)).thenReturn(Optional.of(ftpaApplicationDate));
    }

}
