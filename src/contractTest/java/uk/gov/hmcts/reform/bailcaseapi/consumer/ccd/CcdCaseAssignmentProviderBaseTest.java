package uk.gov.hmcts.reform.bailcaseapi.consumer.ccd;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;

import java.io.IOException;

import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(locations = {"classpath:application.properties"})
public class CcdCaseAssignmentProviderBaseTest {

    @MockBean
    AuthTokenGenerator serviceAuthTokenGenerator;
    @MockBean
    UserDetailsProvider userDetailsProvider;
    @Value("${core_case_data_api_assignments_url}")
    String ccdUrl;
    @Value("${assign_case_access_api_url}")
    String aacUrl;
    @Value("${core_case_data_api_assignments_path}")
    String ccdAssignmentsApiPath;
    @Value("${assign_case_access_api_assignments_path}")
    String aacAssignmentsApiPath;
    @Value("${apply_noc_access_api_assignments_path}")
    String applyNocAssignmentsApiPath;

    CcdCaseAssignment ccdCaseAssignment;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final long CASE_ID = 1583841721773828L;
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    static final String IDAM_ID_OF_USER_CREATING_CASE = "0a5874a4-3f38-4bbd-ba4c";

    @Mock
    Callback<BailCase> callback;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    UserDetails userDetails;

    @BeforeEach
    public void setUpTest() {
        ccdCaseAssignment =
            new CcdCaseAssignment(new RestTemplate(), serviceAuthTokenGenerator, userDetailsProvider,
                                  ccdUrl, aacUrl, ccdAssignmentsApiPath,
                aacAssignmentsApiPath, applyNocAssignmentsApiPath);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(AUTHORIZATION_TOKEN);
        when(userDetails.getId()).thenReturn(IDAM_ID_OF_USER_CREATING_CASE);
    }

    protected String createJsonObject(Object obj) throws IOException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
