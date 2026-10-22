package uk.gov.hmcts.reform.iacaseapi.infrastructure.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.HealthContributors;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.HealthCheckConfiguration;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DownStreamHealthIndicatorTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    HealthCheckConfiguration healthCheckConfiguration;

    private DownStreamHealthIndicator downStreamHealthIndicator;

    @Test
    void testGetContributor() {
        when(healthCheckConfiguration.getServices()).thenReturn(getHealthCheckConfiguration());

        downStreamHealthIndicator = new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration);

        assertNotNull(downStreamHealthIndicator.getContributor("service2"));
        assertEquals(ServiceHealthIndicator.class, downStreamHealthIndicator.getContributor("service2").getClass());
    }

    @Test
    void should_throw_exception_when_services_list_is_null_or_empty() {
        when(healthCheckConfiguration.getServices()).thenReturn(null);

        Assertions.assertThatThrownBy(() -> new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration))
            .hasMessage("HealthCheckConfiguration cannot be null or empty")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private Map<String, Map<String, String>> getHealthCheckConfiguration() {

        Map<String, Map<String, String>> services = new HashMap<String, Map<String, String>>();
        services.put("service1", ImmutableMap.of("uri", "http://service1uri", "response", "\"status\":\"UP\""));
        services.put("service2", ImmutableMap.of("uri", "http://service2uri", "response", "\"status\":\"UP\""));

        return services;
    }

    @Test
    void testIterator() {
        when(healthCheckConfiguration.getServices()).thenReturn(getHealthCheckConfiguration());

        downStreamHealthIndicator = new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration);

        Iterator<HealthContributors.Entry> iterator = downStreamHealthIndicator.iterator();

        assertNotNull(iterator);
        int count = 0;
        while (iterator.hasNext()) {
            HealthContributors.Entry entry = iterator.next();
            assertNotNull(entry.name());
            assertNotNull(entry.contributor());
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void testStream() {
        when(healthCheckConfiguration.getServices()).thenReturn(getHealthCheckConfiguration());

        downStreamHealthIndicator = new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration);

        List<HealthContributors.Entry> entries = downStreamHealthIndicator.stream().collect(Collectors.toList());

        assertNotNull(entries);
        assertEquals(2, entries.size());
        Assertions.assertThat(entries)
            .extracting(HealthContributors.Entry::name)
            .containsExactlyInAnyOrder("service1", "service2");
    }

}

