package uk.gov.hmcts.reform.bailcaseapi.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import feign.FeignException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.SystemUserProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CcdDataServiceTest {

    @Mock
    private CcdDataApi ccdDataApi;
    @Mock
    private SystemTokenGenerator systemTokenGenerator;
    @Mock
    private SystemUserProvider systemUserProvider;
    @Mock
    private AuthTokenGenerator serviceAuthorization;

    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> startEventCaseDetails;
    @Mock
    private BailCase startEventBailCase;
    @Mock
    private BailCase caseData;

    private String token = "Bearer token";
    private String serviceToken = "Bearer serviceToken";
    private String userId = "userId";
    private String eventToken = "eventToken";
    private long caseId = 1234;
    private String jurisdiction = "IA";
    private String caseType = "Bail";
    private String eventId = "clearLegalRepresentativeDetails";

    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {

        ccdDataService =
            new CcdDataService(ccdDataApi, systemTokenGenerator, systemUserProvider, serviceAuthorization);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getJurisdiction()).thenReturn(jurisdiction);

        when(startEventCaseDetails.getCaseData()).thenReturn(startEventBailCase);
        when(startEventBailCase.read(BailCaseFieldDefinition.LEGAL_REP_NAME, String.class))
            .thenReturn(Optional.of("J Smith"));
        when(startEventBailCase.read(BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));
        when(startEventBailCase.read(BailCaseFieldDefinition.LEGAL_REP_PHONE, String.class))
            .thenReturn(Optional.of("0123654"));
        when(startEventBailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class))
            .thenReturn(Optional.of("A123"));
        when(startEventBailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of("smith@test.com"));
        when(startEventBailCase.read(BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));


        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId(token)).thenReturn(userId);
    }

    @Test
    void service_should_throw_on_unable_to_generate_system_user_token() {
        when(systemTokenGenerator.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.clearLegalRepDetails(callback));
    }

    @Test
    void service_should_throw_on_unable_to_generate_s2s_token() {
        when(systemTokenGenerator.generate()).thenReturn("aSystemUserToken");
        when(serviceAuthorization.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.clearLegalRepDetails(callback));
    }

    @Test
    void service_should_clear_the_lr_details_for_the_case_id() {
        StartEventDetails startEventResponse = getStartEventResponse();
        when(ccdDataApi.startEvent(
            token, serviceToken, userId, jurisdiction, caseType,
            String.valueOf(caseId), eventId
        )).thenReturn(startEventResponse);

        // set CaseData for the subEvent request
        CaseDataContent caseDataContent = getCaseDataContent();
        when(ccdDataApi.submitEvent(
            token, serviceToken, String.valueOf(caseId),
            caseDataContent
        )).thenReturn(getSubmitEventResponse());

        SubmitEventDetails submitEventDetails =
            ccdDataService.clearLegalRepDetails(callback);

        assertNotNull(submitEventDetails);
        assertEquals(caseId, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertLegalRepDetailsClear(submitEventDetails.getData());
        assertEquals("CALLBACK_COMPLETED", submitEventDetails.getCallbackResponseStatus());

        verify(ccdDataApi, times(1))
            .startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, String.valueOf(caseId), eventId
            );
        verify(ccdDataApi, times(1))
            .submitEvent(token, serviceToken, String.valueOf(caseId), caseDataContent);
    }

    @ParameterizedTest
    @CsvSource({
        "LEGAL_REP_NAME",
        "LEGAL_REP_EMAIL_ADDRESS",
        "LEGAL_REP_PHONE",
        "LEGAL_REP_COMPANY",
        "LEGAL_REP_COMPANY_ADDRESS",
        "LEGAL_REP_REFERENCE"
    })
    void service_should_not_error_when_part_of_legal_rep_details_are_present(BailCaseFieldDefinition bailCaseFieldDefinition) {

        when(startEventBailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_PHONE, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUK.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        if (bailCaseFieldDefinition.equals(LEGAL_REP_COMPANY_ADDRESS)) {
            when(startEventBailCase.read(bailCaseFieldDefinition, AddressUK.class))
                .thenReturn(Optional.of(new AddressUK(
                    "Line1",
                    "Line2",
                    "Line3",
                    "Town",
                    "County",
                    "Postcode",
                    "Country"
                )));
        } else {
            when(startEventBailCase.read(bailCaseFieldDefinition, String.class))
                .thenReturn(Optional.of("Some Value"));
        }

        StartEventDetails startEventResponse = getStartEventResponse();
        when(
            ccdDataApi.startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, String.valueOf(caseId), eventId
            )).thenReturn(startEventResponse);

        assertDoesNotThrow(() -> ccdDataService.clearLegalRepDetails(callback));

        verify(ccdDataApi, times(1))
            .startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, String.valueOf(caseId), eventId
            );
    }

    @Test
    void service_should_error_legal_rep_details_are_not_present() {

        when(startEventBailCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUK.class)).thenReturn(Optional.empty());
        when(startEventBailCase.read(LEGAL_REP_PHONE, String.class)).thenReturn(Optional.empty());

        StartEventDetails startEventResponse = getStartEventResponse();
        when(
            ccdDataApi.startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, String.valueOf(caseId), eventId
            )).thenReturn(startEventResponse);

        ResponseStatusException rse = assertThrows(
            ResponseStatusException.class, () ->
                ccdDataService.clearLegalRepDetails(callback)
        );

        assertThat(rse.getMessage()).contains("400 BAD_REQUEST \"Legal Rep Details not found for the caseId: 1234\"");

        verify(ccdDataApi, times(1))
            .startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, String.valueOf(caseId), eventId
            );
    }

    @Test
    void service_should_error_on_invalid_ccd_case_reference() {
        when(
            ccdDataApi.startEvent(
                token, serviceToken, userId,
                jurisdiction, caseType, String.valueOf(caseId), eventId
            )).thenThrow(FeignException.class);

        assertThatThrownBy(() -> ccdDataService.clearLegalRepDetails(callback))
            .isExactlyInstanceOf(FeignException.class);

    }

    private void assertLegalRepDetailsClear(Map<String, Object> data) {
        assertEquals("", data.get(LEGAL_REP_NAME.value()));
        assertEquals("", data.get(LEGAL_REP_FAMILY_NAME.value()));
        assertEquals("", data.get(LEGAL_REP_COMPANY.value()));
        assertEquals("", data.get(LEGAL_REP_PHONE.value()));
        assertEquals("", data.get(LEGAL_REP_REFERENCE.value()));
        assertEquals("", data.get(LEGAL_REP_COMPANY_ADDRESS.value()));
        assertEquals("", data.get(LEGAL_REP_EMAIL_ADDRESS.value()));
        assertEquals("No", data.get(IS_LEGALLY_REPRESENTED_FOR_FLAG.value()));
    }

    private StartEventDetails getStartEventResponse() {
        return new StartEventDetails(Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS, eventToken, startEventCaseDetails);
    }

    private SubmitEventDetails getSubmitEventResponse() {
        return new SubmitEventDetails(
            caseId, jurisdiction, State.DECISION_DECIDED, getDataWithEmptyLegalRepDetails(),
            200, "CALLBACK_COMPLETED"
        );
    }

    private CaseDataContent getCaseDataContent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS.toString());

        return new CaseDataContent(
            String.valueOf(caseId), getDataWithEmptyLegalRepDetails(),
            eventData, eventToken, true
        );
    }

    private Map<String, Object> getDataWithEmptyLegalRepDetails() {
        Map<String, Object> data = new HashMap<>();
        data.put(LEGAL_REP_NAME.value(), "");
        data.put(LEGAL_REP_FAMILY_NAME.value(), "");
        data.put(LEGAL_REP_COMPANY.value(), "");
        data.put(LEGAL_REP_PHONE.value(), "");
        data.put(LEGAL_REP_REFERENCE.value(), "");
        data.put(LEGAL_REP_COMPANY_ADDRESS.value(), "");
        data.put(LEGAL_REP_EMAIL_ADDRESS.value(), "");
        data.put(IS_LEGALLY_REPRESENTED_FOR_FLAG.value(), "No");
        return data;
    }

    @Test
    void should_set_active_interpreter_flag_to_yes_when_active_flag_present() {
        CaseFlagValue caseFlagValue = CaseFlagValue.builder().name("Interpreter").status("Active").build();
        CaseFlagDetail caseFlagDetail = new CaseFlagDetail("someId", caseFlagValue);
        StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(
            "someFullName",
            "someRole",
            List.of(caseFlagDetail)
        );
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(caseData.read(BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(strategicCaseFlag));
        StartEventDetails startEventDetails = new StartEventDetails(
            Event.UPDATE_INTERPRETER_WA_TASK,
            eventToken,
            caseDetails
        );
        when(ccdDataApi.startEvent(
            token,
            serviceToken,
            userId,
            jurisdiction,
            "Bail",
            String.valueOf(caseId),
            Event.UPDATE_INTERPRETER_WA_TASK.toString()
        ))
            .thenReturn(startEventDetails);
        SubmitEventDetails submitEventDetails = new SubmitEventDetails(
            caseId,
            jurisdiction,
            State.DECISION_DECIDED,
            Map.of("hasActiveInterpreterFlag", "Yes"),
            200,
            "CALLBACK_COMPLETED"
        );
        when(ccdDataApi.submitEvent(any(), any(), any(), any())).thenReturn(submitEventDetails);

        SubmitEventDetails result = ccdDataService.setActiveInterpreterFlag(callback);

        assertEquals("Yes", result.getData().get("hasActiveInterpreterFlag"));
    }

    @Test
    void should_set_active_interpreter_flag_to_no_when_flag_present_but_inactive() {
        CaseFlagValue caseFlagValue = CaseFlagValue.builder().name("Interpreter").status("Inactive").build();
        CaseFlagDetail caseFlagDetail = new CaseFlagDetail("someId", caseFlagValue);
        StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(
            "someFullName",
            "someRole",
            List.of(caseFlagDetail)
        );
        when(caseDetails.getCaseData()).thenReturn(caseData);

        when(caseData.read(BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(strategicCaseFlag));
        StartEventDetails startEventDetails = new StartEventDetails(
            Event.UPDATE_INTERPRETER_WA_TASK,
            eventToken,
            caseDetails
        );
        when(ccdDataApi.startEvent(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventDetails);
        SubmitEventDetails submitEventDetails = new SubmitEventDetails(
            caseId,
            jurisdiction,
            State.DECISION_DECIDED,
            Map.of("hasActiveInterpreterFlag", "No"),
            200,
            "CALLBACK_COMPLETED"
        );
        when(ccdDataApi.submitEvent(any(), any(), any(), any())).thenReturn(submitEventDetails);

        SubmitEventDetails result = ccdDataService.setActiveInterpreterFlag(callback);

        assertEquals("No", result.getData().get("hasActiveInterpreterFlag"));
    }

    @Test
    void should_set_active_interpreter_flag_to_no_when_no_interpreter_flag_present() {
        CaseFlagValue caseFlagValue = CaseFlagValue.builder().name("OtherFlag").status("Active").build();
        CaseFlagDetail caseFlagDetail = new CaseFlagDetail("someId", caseFlagValue);
        StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(
            "someFullName",
            "someRole",
            List.of(caseFlagDetail)
        );
        when(caseDetails.getCaseData()).thenReturn(caseData);

        when(caseData.read(BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(strategicCaseFlag));
        StartEventDetails startEventDetails = new StartEventDetails(
            Event.UPDATE_INTERPRETER_WA_TASK,
            eventToken,
            caseDetails
        );
        when(ccdDataApi.startEvent(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventDetails);
        SubmitEventDetails submitEventDetails = new SubmitEventDetails(
            caseId,
            jurisdiction,
            State.DECISION_DECIDED,
            Map.of("hasActiveInterpreterFlag", "No"),
            200,
            "CALLBACK_COMPLETED"
        );
        when(ccdDataApi.submitEvent(any(), any(), any(), any())).thenReturn(submitEventDetails);

        SubmitEventDetails result = ccdDataService.setActiveInterpreterFlag(callback);

        assertEquals("No", result.getData().get("hasActiveInterpreterFlag"));
    }

    @Test
    void should_set_active_interpreter_flag_to_no_when_no_flags_exist() {
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(caseData.read(BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.empty());
        StartEventDetails startEventDetails = new StartEventDetails(
            Event.UPDATE_INTERPRETER_WA_TASK,
            eventToken,
            caseDetails
        );
        when(ccdDataApi.startEvent(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventDetails);
        SubmitEventDetails submitEventDetails = new SubmitEventDetails(
            caseId,
            jurisdiction,
            State.DECISION_DECIDED,
            Map.of("hasActiveInterpreterFlag", "No"),
            200,
            "CALLBACK_COMPLETED"
        );
        when(ccdDataApi.submitEvent(any(), any(), any(), any())).thenReturn(submitEventDetails);

        SubmitEventDetails result = ccdDataService.setActiveInterpreterFlag(callback);

        assertEquals("No", result.getData().get("hasActiveInterpreterFlag"));
    }
}
