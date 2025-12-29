package uk.gov.hmcts.reform.bailcaseapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.CommonDataRefApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    CommonDataRefApi.class,
    LocationRefDataApi.class
})
@PropertySource("classpath:application.properties")
public class RefDataConsumerApplication {

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    RestTemplate restTemplate;
}
