package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EditAppellantPersonalDataHandlerTest {

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
    private DateProvider dateProvider;
    @Mock
    private DueDateService dueDateService;
    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private DocumentWithDescription appealWasNotSubmitted;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "30/01/2019";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "31/01/2019";
    private String applicationStatus = "In progress";
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractor;
    @Captor
    private ArgumentCaptor<YesOrNo> outOfTime;
    @Captor
    private ArgumentCaptor<YesOrNo> recordedOutOfTimeDecision;
    @Captor
    private ArgumentCaptor<YesOrNo> hasAddedLegalRepDetails;

    private List<IdValue<Application>> applications = List.of(new IdValue<>("1", new Application(
            emptyList(),
            applicationSupplier,
            ApplicationType.EDIT_APPEAL_AFTER_SUBMIT.toString(),
            applicationReason,
            applicationDate,
            applicationDecision,
            applicationDecisionReason,
            applicationDateOfDecision,
            applicationStatus
        )),
        new IdValue<>("2", new Application(
            emptyList(),
            applicationSupplier,
            ApplicationType.UPDATE_HEARING_REQUIREMENTS.toString(),
            applicationReason,
            applicationDate,
            applicationDecision,
            applicationDecisionReason,
            applicationDateOfDecision,
            applicationStatus
        )));

    private EditAppellantPersonalDataHandler editAppellantPersonalData;

    @BeforeEach
    public void setUp() {
        editAppellantPersonalData = new EditAppellantPersonalDataHandler();

        when(callback.getEvent()).thenReturn(Event.EDIT_APPELLANT_PERSONAL_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_RESPONDENT_EVIDENCE));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPELLANT_PERSONAL_DATA"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(editAppellantPersonalData.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPELLANT_PERSONAL_DATA"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_bad_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(editAppellantPersonalData.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_bad_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPELLANT_PERSONAL_DATA);
        assertFalse(editAppellantPersonalData.canHandle(stage, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> editAppellantPersonalData.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppellantPersonalData.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppellantPersonalData.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryDecisionType.class, names = {"REFUSAL_OF_HUMAN_RIGHTS", "REFUSE_PERMIT"})
    void should_write_gwf_reference_number_if_ooc_refusal_of_hu_or_permit(OutOfCountryDecisionType outOfCountryDecisionType) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(outOfCountryDecisionType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeOfficeReferenceNumber"));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(GWF_REFERENCE_NUMBER, "homeOfficeReferenceNumber");
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryDecisionType.class, names = {"REFUSAL_OF_HUMAN_RIGHTS", "REFUSE_PERMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_write_gwf_reference_number_if_ooc_not_refusal_of_hu_or_permit(OutOfCountryDecisionType outOfCountryDecisionType) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(outOfCountryDecisionType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeOfficeReferenceNumber"));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase, never()).write(eq(GWF_REFERENCE_NUMBER), any());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"HU", "EA"})
    void should_write_gwf_reference_number_if_hu_ea_outsideUkWhenApplicationMade(AppealType appealType) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeOfficeReferenceNumber"));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(GWF_REFERENCE_NUMBER, "homeOfficeReferenceNumber");
    }


    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"HU", "EA"})
    void should_not_write_gwf_reference_number_if_hu_ea_not_outsideUkWhenApplicationMade(AppealType appealType) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeOfficeReferenceNumber"));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE, YesOrNo.class)).thenReturn(Optional.of(NO));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase, never()).write(eq(GWF_REFERENCE_NUMBER), any());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"HU", "EA"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_write_gwf_reference_number_if_not_hu_ea_outsideUkWhenApplicationMade(AppealType appealType) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("homeOfficeReferenceNumber"));
        when(asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase, never()).write(eq(GWF_REFERENCE_NUMBER), any());
    }

    @Test
    void should_write_gwf_reference_number_if_no_ooc_decision_type_internal_ooc() {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("gwfReferenceNumber"));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(eq(GWF_REFERENCE_NUMBER), eq("gwfReferenceNumber"));
    }

    @Test
    void changeEditAppealApplicationsToCompleted() {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("gwfReferenceNumber"));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).clear((APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS));
        verify(asylumCase).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.AWAITING_RESPONDENT_EVIDENCE);
        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        List<Application> updatedApplications = applicationsCaptor.getValue().stream()
            .map(IdValue::getValue)
            .toList();

        assertEquals("Completed", updatedApplications.getFirst().getApplicationStatus());
        assertEquals(applicationStatus, updatedApplications.getLast().getApplicationStatus());

    }
}
