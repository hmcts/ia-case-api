package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(RestTemplate restTemplate) {
        return restTemplate;
    }

    @Bean
    public RestTemplate restTemplate(JsonMapper jsonMapper) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().removeIf(JacksonJsonHttpMessageConverter.class::isInstance);
        restTemplate.getMessageConverters().add(new JacksonJsonHttpMessageConverter(jsonMapper));
        return restTemplate;
    }
}
