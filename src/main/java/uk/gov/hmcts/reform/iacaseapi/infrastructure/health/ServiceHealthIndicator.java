package uk.gov.hmcts.reform.iacaseapi.infrastructure.health;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class ServiceHealthIndicator implements HealthIndicator {

    private String uri;
    private String matcher;
    private RestTemplate restTemplate;

    public ServiceHealthIndicator(String uri, String matcher, RestTemplate restTemplate) {
        this.uri = uri;
        this.matcher = matcher;
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {

        try {
            ResponseEntity<String> response = restTemplate
                .getForEntity(uri, String.class);

            String responseBody = Optional
                .ofNullable(response.getBody())
                .map(body -> body.replaceAll("\\s", ""))
                .orElse("");

            log.info("{}", response.getStatusCode());
            if (response.getStatusCode() == HttpStatus.OK
                && responseBody.contains(matcher)
                && !responseBody.contains("DOWN")) {

                return new Health
                    .Builder(Status.UP)
                    .build();
            } else {

                return new Health
                    .Builder(Status.DOWN)
                    .build();
            }
        } catch (RestClientException ex) {

            log.error("Downstream service exception {} calling URI {}", Health.down(ex).build().getDetails(), uri);
            return new Health
                .Builder()
                .down(ex)
                .build();
        }
    }
}
