package uk.gov.hmcts.reform.iacaseapi.consumer;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SpringBootContractBaseTest {

    public static final String PACT_TEST_EMAIL_VALUE = "ia-caseofficer@fake.hmcts.net";
    public static final String PACT_TEST_PASSWORD_VALUE = "London01";
    public static final String PACT_TEST_CLIENT_ID_VALUE = "pact";
    public static final String PACT_TEST_CLIENT_SECRET_VALUE = "pactsecret";
    public static final String PACT_TEST_SCOPES_VALUE = "openid profile roles";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    public static final String SERVICE_AUTH_TOKEN = "Bearer someServiceAuthorizationToken";


    public HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN);
        headers.add(AUTHORIZATION, AUTH_TOKEN);
        return headers;
    }
}