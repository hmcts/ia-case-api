package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class ProfessionalUsersRetrieverTest {

    private ProfessionalUsersRetriever professionalUsersRetriever;

    private String refdataUrl = "http:/some-url";
    private String refdataPath = "/some-path";

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserDetails userDetails;
    @Mock private RestTemplate restTemplate;
    @Mock private ResponseEntity responseEntity;

    @BeforeEach
    public void setup() {

        professionalUsersRetriever = new ProfessionalUsersRetriever(restTemplate,
            serviceAuthTokenGenerator,
            userDetails,
            refdataUrl,
            refdataPath);

    }

    @Test
    void should_successfully_get_prof_users_response() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final ProfessionalUsersResponse professionalUsersResponse =
            mock(ProfessionalUsersResponse.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(userDetails.getAccessToken()).thenReturn(expectedAccessToken);

        doReturn(responseEntity)
            .when(restTemplate)
            .exchange(
                eq(refdataUrl + refdataPath),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        when(responseEntity.getBody()).thenReturn(professionalUsersResponse);

        ProfessionalUsersResponse response = professionalUsersRetriever.retrieve();

        assertThat(response).isNotNull();
        assertThat(response).isInstanceOf(ProfessionalUsersResponse.class);

        verify(restTemplate).exchange(anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(professionalUsersResponse);

    }

    public void wraps_http_exception_correctly_when_calling_prof_ref_data() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(userDetails.getAccessToken()).thenReturn(expectedAccessToken);

        final RestClientResponseException restException = mock(RestClientResponseException.class);
        doThrow(restException)
            .when(restTemplate)
            .exchange(
                eq(refdataUrl + refdataPath),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        assertThatThrownBy(() -> professionalUsersRetriever.retrieve())
            .isInstanceOf(ReferenceDataIntegrationException.class)
            .hasCauseInstanceOf(RestClientResponseException.class)
            .hasCause(restException);

        verify(restTemplate).exchange(anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class));

    }

}
