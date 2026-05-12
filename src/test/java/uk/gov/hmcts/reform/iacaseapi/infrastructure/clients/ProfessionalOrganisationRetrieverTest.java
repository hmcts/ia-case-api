package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProfessionalOrganisationRetrieverTest {

    private ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    private String refdataUrl = "http:/some-url";
    private String refdataPath = "/some-path";

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ResponseEntity responseEntity;

    @BeforeEach
    public void setup() {

        professionalOrganisationRetriever = new ProfessionalOrganisationRetriever(restTemplate,
            serviceAuthTokenGenerator,
            userDetailsProvider,
            refdataUrl,
            refdataPath);

    }

    @Test
    public void should_successfully_get_prof_orgaisation_response() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final OrganisationEntityResponse organisationResponse =
            mock(OrganisationEntityResponse.class);
        final UserDetails userDetails = mock(UserDetails.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(expectedAccessToken);

        doReturn(responseEntity)
            .when(restTemplate)
            .exchange(
                eq(refdataUrl + refdataPath),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        when(responseEntity.getBody()).thenReturn(organisationResponse);

        OrganisationEntityResponse response = professionalOrganisationRetriever.retrieve();

        assertThat(response).isNotNull().isInstanceOf(OrganisationEntityResponse.class);

        verify(restTemplate).exchange(anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(organisationResponse);

    }
}
