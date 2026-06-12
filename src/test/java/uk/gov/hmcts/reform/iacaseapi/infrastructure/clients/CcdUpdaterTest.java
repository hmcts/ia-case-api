package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CcdUpdaterTest {

    private static final String SERVICE_TOKEN = "ABCDEF";
    private static final String ACCESS_TOKEN = "12345";
    private static final String IDAM_ID_OF_USER_SHARING_CASE = "TEST_ID_SHARING_ACCESS";
    private static final String IDAM_ID_OF_USER_GETTING_ACCESS_TO_CASE = "TEST_ID_GETTING_ACCESS";
    private CcdUpdater ccdUpdater;
    private String ccdUrl = "some-host";
    private String ccdPermissionsApiPath = "some-path";

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private RestTemplate restTemplate;
    @Mock private ResponseEntity<Object> responseEntity;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;

    @BeforeEach
    public void setUp() {

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_SHARING_CASE);

        ccdUpdater = new CcdUpdater(
            restTemplate,
            serviceAuthTokenGenerator,
            userDetails,
            ccdUrl,
            ccdPermissionsApiPath);
    }

    @Test
    void should_sent_post_to_update_ccd_and_receive_201() {

        Value value1 = new Value("another-user-id", "email@somewhere.com");
        Value value2 = new Value(IDAM_ID_OF_USER_GETTING_ACCESS_TO_CASE, "email@somewhere.com");

        List<Value> values = Lists.newArrayList(value1, value2);
        DynamicList dynamicList = new DynamicList(value2, values);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS))
            .thenReturn(Optional.of(dynamicList));
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getJurisdiction()).thenReturn("ia");

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenReturn(responseEntity);

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CREATED);

        ccdUpdater.updatePermissions(callback);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );

    }

    @Test
    void should_handle_when_rest_exception_thrown() {

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);

        Value value1 = new Value("another-user-id", "email@somewhere.com");
        Value value2 = new Value(IDAM_ID_OF_USER_GETTING_ACCESS_TO_CASE, "email@somewhere.com");

        List<Value> values = Lists.newArrayList(value1, value2);
        DynamicList dynamicList = new DynamicList(value2, values);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS))
            .thenReturn(Optional.of(dynamicList));
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getJurisdiction()).thenReturn("ia");

        when(restTemplate
            .exchange(
                eq(ccdUrl + ccdPermissionsApiPath),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenThrow(restClientResponseEx);

        assertThatThrownBy(() -> ccdUpdater.updatePermissions(callback))
            .isInstanceOf(CcdDataIntegrationException.class)
            .hasMessage("Couldn't update CCD case permissions using API: "
                + ccdUrl
                + ccdPermissionsApiPath)
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
    void should_throw_when_there_is_no_org_list_of_users() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ccdUpdater.updatePermissions(callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS
                + " is empty in case data when required.");

    }

    @Test
    void should_throw_when_callback_param_is_null() {

        assertThatThrownBy(() -> ccdUpdater.updatePermissions(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
