package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {
    @Bean
    public ObjectMapper getMapper() {
        return JsonMapper.builder()
                .disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)
                .build();
    }

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
        HttpClient httpClient = HttpClients.custom()
                .disableContentCompression()  // Optional: prevent double compression issues
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(30_000);

        RestTemplate restTemplate = new RestTemplate(factory);

        return restTemplate;
        // RestTemplate restTemplate = new RestTemplate();
        // restTemplate.getMessageConverters().removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        // restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter(objectMapper));

        // return restTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
        ObjectMapper objectMapper
    ) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

}
