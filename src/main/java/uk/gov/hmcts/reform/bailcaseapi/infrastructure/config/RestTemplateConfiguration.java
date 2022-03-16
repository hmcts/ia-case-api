package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        ObjectMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(
        ObjectMapper objectMapper
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
            .removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter(objectMapper));

        return restTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
        ObjectMapper objectMapper
    ) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

}
