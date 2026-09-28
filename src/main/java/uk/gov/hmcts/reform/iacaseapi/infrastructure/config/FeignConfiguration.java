package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("removal")
public class FeignConfiguration {

    @Bean
    public HttpMessageConverterCustomizer feignJacksonConverterCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return converters -> {
            converters.removeIf(c -> c instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter);
            converters.addFirst(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
        };
    }

    @Bean
    public ClientHttpMessageConvertersCustomizer jacksonClientCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return builder -> builder.withJsonConverter(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Bean
    @Qualifier("feign")
    public ObjectMapper feignObjectMapper(org.springframework.http.converter.json.Jackson2ObjectMapperBuilder builder) {
        return builder
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .modules(
                new Jdk8Module(),
                new JavaTimeModule()
            )
            .build();
    }
}
