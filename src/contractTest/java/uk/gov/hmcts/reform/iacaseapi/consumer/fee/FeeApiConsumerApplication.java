package uk.gov.hmcts.reform.iacaseapi.consumer.fee;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeesRegisterApi;


@SpringBootApplication
@EnableFeignClients(clients = {
    FeesRegisterApi.class
})
public class FeeApiConsumerApplication {

    @MockBean
    RestTemplate restTemplate;
}
