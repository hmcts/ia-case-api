package uk.gov.hmcts.reform.bailcaseapi.consumer.ccd;

import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(locations = {"classpath:application.properties"})
public class CcdSupplementaryProviderBaseTest {

    @MockBean
    AuthTokenGenerator serviceAuthTokenGenerator;
    @MockBean
    UserDetailsProvider userDetailsProvider;
    @Value("${core_case_data_supplementary_api_url}")
    String ccdUrl;
    @Value("${core_case_data_supplementary_api_path}")
    String supplementaryUrl;
    @Value("${hmcts_service_id}")
    String hmctsServiceId;

    CcdSupplementaryUpdater ccdSupplementaryUpdater;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final long CASE_ID = 1583841721778873L;
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Mock
    Callback<BailCase> callback;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    UserDetails userDetails;

    @BeforeEach
    public void setUpTest() {
        String urlPath = UriComponentsBuilder.fromPath(supplementaryUrl).build(CASE_ID).toString();

        ccdSupplementaryUpdater =
            new CcdSupplementaryUpdater(new RestTemplate(), serviceAuthTokenGenerator, userDetails, ccdUrl,
                    urlPath, hmctsServiceId);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(userDetails.getAccessToken()).thenReturn(AUTHORIZATION_TOKEN);
    }

    protected String createJsonObject(Object obj) throws IOException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
