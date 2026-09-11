package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        JsonMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(
        JsonMapper objectMapper
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().removeIf(converter -> converter instanceof JacksonJsonHttpMessageConverter);
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter(objectMapper));

        return restTemplate;
    }

    @Bean
    public JacksonJsonHttpMessageConverter mappingJackson2HttpMessageConverter(
        JsonMapper objectMapper
    ) {
        return new JacksonJsonHttpMessageConverter(objectMapper);
    }

}
