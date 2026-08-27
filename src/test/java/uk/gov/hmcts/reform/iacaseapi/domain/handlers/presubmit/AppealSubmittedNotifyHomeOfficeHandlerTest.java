package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.getMismatchErrorMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppealSubmittedNotifyHomeOfficeHandlerTest {

    private static final String VALID_GWF = "GWF123456789";
    private static final String APPEAL_REF = "PA/12345/2025";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String DOB = "1990-01-01";
    private final String decryptedData =
        "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]";

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private HomeOfficeReferenceService hoReferenceService;

    @Mock
    private HomeOfficeAppellant appellant;

    @Captor
    private ArgumentCaptor<List<IdValue<HomeOfficeAppellant>>> listCaptor;

    private AppealSubmittedNotifyHomeOfficeHandler handler;

    private final String key = "0fbb3d6ce00b08209f24609a6766d50ef293419eb8362ea435bdd11994ba97e8";

    @BeforeEach
    void setup() {

        handler = new AppealSubmittedNotifyHomeOfficeHandler(true, hoReferenceService, homeOfficeApi, key);

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(caseDetails.getId()).thenReturn(12345L);
    }

    @Test
    void getDispatchPriority_should_return_last() {

        assertEquals(DispatchPriority.LAST, handler.getDispatchPriority());
    }

    @Test
    void canHandle_should_return_true_for_appeal_started() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_true_for_appeal_started_by_admin() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED_BY_ADMIN);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {

        assertFalse(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_throw_when_callback_stage_is_null() {

        assertThrows(NullPointerException.class, () -> handler.canHandle(null, callback));
    }

    @Test
    void canHandle_should_throw_when_callback_is_null() {

        assertThrows(NullPointerException.class, () ->
            handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
    }

    @Test
    void handle_should_throw_when_cannot_handle() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_should_throw_when_home_office_and_gwf_references_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
            .thenReturn(Optional.of("string"));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
            ex.getMessage());
    }

    @Test
    void handle_should_throw_when_appeal_reference_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
            .thenReturn(Optional.of("string"));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("Case ID for the appeal is not present", ex.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = HomeOfficeApiResponseStatusType.class, names = {"OK"}, mode = EnumSource.Mode.EXCLUDE)
    void handle_should_return_errors_if_reference_validation_fails(HomeOfficeApiResponseStatusType status) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
            .thenReturn(Optional.of("string"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));
        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(status));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors())
            .hasSize(1)
            .contains(status.getUserFacingErrorText(VALID_GWF));
    }

    @Test
    void handle_should_return_errors_if_other_validation_fails() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
            .thenReturn(Optional.of("string"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));
        when(hoReferenceService.getHomeOfficeReferenceData(VALID_GWF, callback)).thenReturn(List.of(new IdValue<>("id", appellant)));
        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors())
            .hasSize(1)
            .contains(getMismatchErrorMessage(VALID_GWF, false, true));
    }

    @Test
    void handle_should_notify_home_office_and_return_updated_case() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
            .thenReturn(Optional.of("string"));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));

        when(hoReferenceService.getHomeOfficeReferenceData(VALID_GWF, callback)).thenReturn(List.of(new IdValue<>("id", appellant)));
        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));
        when(appellant.getGivenNames()).thenReturn(FIRST_NAME);
        when(appellant.getFamilyName()).thenReturn(LAST_NAME);
        when(appellant.getDateOfBirth()).thenReturn(DOB);
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(FIRST_NAME));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(LAST_NAME));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(DOB));

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(homeOfficeApi).aboutToSubmit(callback);
        verify(asylumCase, times(2)).clear(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY);
        verify(asylumCase).write(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.YES);
    }

    @Test
    void handle_should_use_gwf_reference_when_home_office_reference_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
            .thenReturn(Optional.of("string"));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));

        when(hoReferenceService.getHomeOfficeReferenceData(VALID_GWF, callback)).thenReturn(List.of(new IdValue<>("id", appellant)));
        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));
        when(appellant.getGivenNames()).thenReturn(FIRST_NAME);
        when(appellant.getFamilyName()).thenReturn(LAST_NAME);
        when(appellant.getDateOfBirth()).thenReturn(DOB);
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(FIRST_NAME));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(LAST_NAME));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(DOB));

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verify(homeOfficeApi).aboutToSubmit(callback);
        verify(asylumCase, times(2)).clear(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY);
        verify(asylumCase).write(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.YES);
    }

    @Test
    void should_write_to_home_office_appellants_if_empty() {
        MockedStatic<HandlerUtils> handlerUtilsMock = mockStatic(HandlerUtils.class);
        handlerUtilsMock.when(
                () -> HandlerUtils.decrypt(any(), eq(key)))
            .thenReturn(decryptedData);
        handlerUtilsMock.when(
                () -> HandlerUtils.getUanOrGwf(asylumCase))
            .thenReturn(VALID_GWF);
        handlerUtilsMock.when(
                () -> HandlerUtils.validateAllDetails(callback, asylumCase, VALID_GWF, hoReferenceService))
            .thenReturn(new PreSubmitCallbackResponse<>(asylumCase));
        handler = new AppealSubmittedNotifyHomeOfficeHandler(true, hoReferenceService, homeOfficeApi, key);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));
        when(hoReferenceService.getHomeOfficeReferenceData(VALID_GWF, callback)).thenReturn(List.of(new IdValue<>("id", appellant)));
        when(asylumCase.read(HOME_OFFICE_APPELLANTS, List.class)).thenReturn(Optional.empty());
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANTS), listCaptor.capture());

        List<IdValue<HomeOfficeAppellant>> valueList = listCaptor.getValue();
        assertEquals(1, valueList.size());
        assertEquals("1", valueList.getFirst().getId());
        assertEquals("Smith", valueList.getFirst().getValue().getFamilyName());
    }
}