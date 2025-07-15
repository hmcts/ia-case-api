package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

import java.util.Map;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class CcdCaseAssignmentTest {

    private CcdCaseAssignment ccdCaseAssignment;

    private static final String SERVICE_TOKEN = "ABCDEF";
    private static final String ACCESS_TOKEN = "12345";
    private static final String IDAM_ID_OF_USER_CREATING_CASE = "TEST_ID_CREATING_CASE";
    private final String ccdUrl = "some-host";
    private final String aacUrl = "some-aac-host";
    private final String ccdAssignmentsApiPath = "some-path";
    private final String aacAssignmentsApiPath = "some-aac-path";
    private final String nocAssignmentsApiPath = "some-noc-path";

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private RestTemplate restTemplate;
    @Mock private ResponseEntity<Object> responseEntity;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private UserDetails userDetails;

    @BeforeEach
    public void setUp() {

        ccdCaseAssignment = new CcdCaseAssignment(
            restTemplate,
            serviceAuthTokenGenerator,
            userDetailsProvider,
            ccdUrl,
            aacUrl,
            ccdAssignmentsApiPath,
            aacAssignmentsApiPath,
            nocAssignmentsApiPath);
    }

    @Test
    void should_send_post_to_assign_ccd_case_and_receive_201() {

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenReturn(responseEntity);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CREATED);

        ccdCaseAssignment.assignAccessToCase(callback);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_send_delete_to_revoke_ccd_case_access_and_receive_204() {

        setUpRevokeCcdCaseAccess();
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        ccdCaseAssignment.revokeAccessToCase(callback, "some-org-identifier");
        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_send_delete_to_revoke_legal_rep_access_to_case_and_receive_204() {

        setUpRevokeCcdCaseAccess();

        ccdCaseAssignment.revokeLegalRepAccessToCase(123L, UUID.randomUUID().toString(), "some-org-identifier");
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.DELETE),
            any(HttpEntity.class),
            eq(Object.class)
        );
    }

    private void setUpRevokeCcdCaseAccess() {
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenReturn(responseEntity);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_send_post_to_apply_noc_and_receive_201() {

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenReturn(responseEntity);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CREATED);

        ccdCaseAssignment.applyNoc(callback);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_set_access() {

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                eq(aacUrl + aacAssignmentsApiPath),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenThrow(restClientResponseEx);

        assertThatThrownBy(() -> ccdCaseAssignment.assignAccessToCase(callback))
            .isInstanceOf(CcdDataIntegrationException.class)
            .hasMessage("Couldn't set initial AAC case assignment for case ["
                        + caseDetails.getId()
                        + "] using API: "
                        + aacUrl
                        + aacAssignmentsApiPath)
            .hasCauseInstanceOf(RestClientResponseException.class);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_revoke_access() {

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                eq(ccdUrl + ccdAssignmentsApiPath),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenThrow(restClientResponseEx);

        assertThatThrownBy(() -> ccdCaseAssignment.revokeAccessToCase(callback, "some-org-identifier"))
            .isInstanceOf(CcdDataIntegrationException.class)
            .hasMessage("Couldn't revoke CCD case access for case ["
                        + caseDetails.getId()
                        + "] using API: "
                        + ccdUrl + ccdAssignmentsApiPath)
            .hasCauseInstanceOf(RestClientResponseException.class);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_apply_noc() {

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                eq(aacUrl + nocAssignmentsApiPath),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenThrow(restClientResponseEx);

        assertThatThrownBy(() -> ccdCaseAssignment.applyNoc(callback))
            .isInstanceOf(CcdDataIntegrationException.class)
            .hasMessage("Couldn't apply noc AAC case assignment for case ["
                        + caseDetails.getId()
                        + "] using API: "
                        + aacUrl + nocAssignmentsApiPath)
            .hasCauseInstanceOf(RestClientResponseException.class);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_throw_when_callback_param_is_null() {

        assertThatThrownBy(() -> ccdCaseAssignment.assignAccessToCase(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void payloads_not_empty() {
        Map<String, Object> buildRevokeAccessPayload = ccdCaseAssignment.buildRevokeAccessPayload("some-org-identifier", 123L, IDAM_ID_OF_USER_CREATING_CASE);
        Map<String, Object> buildAssignAccessCaseUserMap = ccdCaseAssignment.buildAssignAccessCaseUserMap(123L, IDAM_ID_OF_USER_CREATING_CASE);
        assertThat(buildRevokeAccessPayload).isNotEmpty();
        assertThat(buildAssignAccessCaseUserMap).isNotEmpty();
    }
}
