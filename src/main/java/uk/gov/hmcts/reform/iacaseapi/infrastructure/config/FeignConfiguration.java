package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@SuppressWarnings("removal")
public class FeignConfiguration {

    @Bean
    public HttpMessageConverterCustomizer feignJacksonConverterCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return converters -> {
            log.info("feign converters BEFORE: {}", converters.stream().map(c -> c.getClass().getSimpleName()).toList());
            converters.removeIf(c -> c instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter);
            converters.add(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
            log.info("feign converters AFTER: {}", converters.stream().map(c -> c.getClass().getSimpleName()).toList());
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
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .modulesToInstall(
                new Jdk8Module(),
                new JavaTimeModule()
            )
            .build();
    }

    // TEMP for test logs
    @Bean
    ApplicationRunner dumpFeignConverters(ObjectProvider<FeignHttpMessageConverters> p) {
        return args -> p.ifAvailable(c ->
                                         c.getConverters().forEach(x -> log.info("feign converter: {}", x.getClass())));
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}
