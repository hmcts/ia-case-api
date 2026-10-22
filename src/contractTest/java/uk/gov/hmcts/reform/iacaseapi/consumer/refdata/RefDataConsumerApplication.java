package uk.gov.hmcts.reform.iacaseapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.CommonDataRefApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.LocationRefDataApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.RefDataCaseWorkerApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

@SpringBootApplication
@EnableFeignClients(clients = {
    RefDataCaseWorkerApi.class,
    CommonDataRefApi.class,
    LocationRefDataApi.class
})
@PropertySource("classpath:application.properties")
@Import(FeignConfiguration.class)
public class RefDataConsumerApplication {

    @MockitoBean
    AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    RestTemplate restTemplate;
}
