package uk.gov.hmcts.reform.iacaseapi.infrastructure.health;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.contributor.HealthContributors;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.HealthCheckConfiguration;

@Slf4j
@Component
public class DownStreamHealthIndicator implements CompositeHealthContributor {

    private final RestTemplate restTemplate;

    private final HealthCheckConfiguration healthCheckConfiguration;

    private Map<String, HealthContributor> contributors = new HashMap<>();

    public DownStreamHealthIndicator(
        RestTemplate restTemplate,
        HealthCheckConfiguration healthCheckConfiguration
    ) {
        this.restTemplate = restTemplate;
        this.healthCheckConfiguration = healthCheckConfiguration;

        try {

            healthCheckConfiguration.getServices().entrySet().stream()
                .forEach(s -> {
                    contributors
                        .put(s.getKey(), new ServiceHealthIndicator(
                            s.getValue().get("uri"),
                            s.getValue().get("response"),
                            restTemplate));
                });
        } catch (NullPointerException ex) {

            log.error("HealthCheckConfiguration cannot be null or empty");
            throw new NullPointerException("HealthCheckConfiguration cannot be null or empty");
        }
    }

    @Override
    public HealthContributor getContributor(String name) {
        return contributors.get(name);
    }

    @Override
    public Iterator<Entry> iterator() {
        return contributors.entrySet().stream()
            .map((entry) -> new HealthContributors.Entry(entry.getKey(), entry.getValue())).iterator();
    }

    @Override
    public Stream<Entry> stream() {
        return this.contributors.entrySet()
            .stream()
            .map(entry -> new HealthContributors.Entry(entry.getKey(), entry.getValue()));
    }


}
