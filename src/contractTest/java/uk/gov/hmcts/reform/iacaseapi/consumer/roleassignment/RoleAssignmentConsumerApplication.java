package uk.gov.hmcts.reform.iacaseapi.consumer.roleassignment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment.RoleAssignmentApi;


@SpringBootApplication
@EnableFeignClients(clients = {
    RoleAssignmentApi.class
})
public class RoleAssignmentConsumerApplication {

    @MockBean
    RestTemplate restTemplate;
}
