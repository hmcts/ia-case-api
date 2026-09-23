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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

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
    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private final String applicationSupplier = "Legal representative";
    private final String applicationReason = "applicationReason";
    private final String applicationDate = "30/01/2019";
    private final String applicationDecision = "Granted";
    private final String applicationDecisionReason = "Granted";
    private final String applicationDateOfDecision = "31/01/2019";
    private final String applicationStatus = "In progress";

    private final List<IdValue<Application>> applications = List.of(new IdValue<>("1", new Application(
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

    @Test
    void should_write_gwf_if_existing() {
        String gwfRef = "GWF-123-456-789";
        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(gwfRef));
        String hoRef = "someUan";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(hoRef));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).read(GWF_REFERENCE_NUMBER, String.class);
        verify(asylumCase).read(HOME_OFFICE_REFERENCE_NUMBER, String.class);
        verify(asylumCase).write(GWF_REFERENCE_NUMBER, hoRef);
    }

    @Test
    void should_not_write_gwf_if_no_existing() {
        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("hoRef"));

        PreSubmitCallbackResponse<AsylumCase> response =
            editAppellantPersonalData.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).read(GWF_REFERENCE_NUMBER, String.class);
        verify(asylumCase, never()).read(HOME_OFFICE_REFERENCE_NUMBER, String.class);
        verify(asylumCase, never()).write(eq(GWF_REFERENCE_NUMBER), anyString());
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
