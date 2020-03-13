package uk.gov.hmcts.reform.iacaseapi.infrastructure.health;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ServiceHealthIndicatorTest {

    @Mock RestTemplate restTemplate;
    @Mock ResponseEntity responseEntity;

    private String uri = "http://docmosis/health";
    private String matcher = "\"status\":\"UP\"";

    private String serviceUpResponse = "{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\"}}}";
    private String serviceDownResponse = "{\"status\":\"DOWN\",\"components\":{\"db\":{\"status\":\"DOWN\"}}}";
    private String statusUpServiceDown = "{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"DOWN\"}}}";
    private String statusUnmatched = "{\"status\":\"UNKNOWN\",\"components\":{\"db\":{\"status\":\"UNKNOWN\"}}}";

    private ServiceHealthIndicator serviceHealthIndicator;

    @Before
    public void setUp() {
        serviceHealthIndicator = new ServiceHealthIndicator(uri, matcher, restTemplate);
    }

    @Test
    public void health_status_should_be_up_when_the_service_is_running() {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(serviceUpResponse);
        when(restTemplate.getForEntity(uri, String.class)).thenReturn(responseEntity);

        assertEquals(Health.up().build(), serviceHealthIndicator.health());
    }

    @Test
    public void health_status_should_show_down_when_the_service_is_not_running() {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(responseEntity.getBody()).thenReturn(serviceDownResponse);
        when(restTemplate.getForEntity(uri, String.class)).thenReturn(responseEntity);

        assertEquals(Health.down().build(), serviceHealthIndicator.health());
    }

    @Test
    public void response_status_ok_but_the_service_is_down() {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(statusUpServiceDown);
        when(restTemplate.getForEntity(uri, String.class)).thenReturn(responseEntity);

        assertEquals(Health.down().build(), serviceHealthIndicator.health());
    }

    @Test
    public void health_should_throw_exception_rest_error() {
        when(restTemplate.getForEntity(uri, String.class)).thenThrow(new RestClientException("Internal server error"));

        assertEquals(Health.down(new RestClientException("Internal server error")).build(), serviceHealthIndicator.health());
    }

    @Test
    public void response_status_ok_but_body_doesnt_match_the_matcher() {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(statusUnmatched);
        when(restTemplate.getForEntity(uri, String.class)).thenReturn(responseEntity);

        assertEquals(Health.down().build(), serviceHealthIndicator.health());
    }
}

