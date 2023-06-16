package uk.gov.hmcts.reform.iacaseapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.iacaseapi.infrastructure.clients"
})
public class CommonRefDataApiConsumerApplication {
}
