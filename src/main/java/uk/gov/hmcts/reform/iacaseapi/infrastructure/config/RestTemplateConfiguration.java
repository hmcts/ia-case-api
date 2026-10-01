package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@SuppressWarnings("removal")
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        ObjectMapper objectMapper
    ) {
        return restTemplate(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();

        // Remove every default JSON converter (Jackson 2 or Jackson 3)
        restTemplate.getMessageConverters().removeIf(converter ->
                                                         converter.getClass().getName().startsWith("org.springframework.http.converter.json.")
                                                             && converter.getClass().getSimpleName().contains("Jackson")
        );

        // Put ours first so it is always the one used
        restTemplate.getMessageConverters().add(0, mappingJackson2HttpMessageConverter(objectMapper));

        log.info("modules: {}, inclusion: {}",
                 objectMapper.getRegisteredModuleIds(),
                 objectMapper.getSerializationConfig().getDefaultPropertyInclusion());

        restTemplate.getMessageConverters()
            .forEach(c -> log.info("converter: {}", c.getClass().getName()));

        return restTemplate;
    }

    @Bean
    public org.springframework.http.converter.json.MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
        ObjectMapper objectMapper
    ) {
        return new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
    }

}
