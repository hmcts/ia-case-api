package uk.gov.hmcts.reform.iacaseapi.consumer.fee;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeesRegisterApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    FeesRegisterApi.class
})
public class FeeApiConsumerApplication {
}
