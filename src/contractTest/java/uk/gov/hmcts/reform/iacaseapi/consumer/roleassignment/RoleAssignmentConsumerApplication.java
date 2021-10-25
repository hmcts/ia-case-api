package uk.gov.hmcts.reform.iacaseapi.consumer.roleassignment;

import feign.Retryer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentApi;


@SpringBootApplication
@EnableFeignClients(clients = {
    RoleAssignmentApi.class
})
public class RoleAssignmentConsumerApplication {

    @MockBean
    RestTemplate restTemplate;

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000L, 1000L, 3);
    }
}
