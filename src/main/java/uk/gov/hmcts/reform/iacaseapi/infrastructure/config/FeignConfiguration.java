package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

@Configuration
public class FeignConfiguration {

    @Bean
    @Primary
    public Encoder feignFormEncoder(
        ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(feignHttpMessageConverters));
    }

    @Bean
    public Decoder decoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverters));
    }

    // A plain HttpMessageConverter bean is all Feign needs now - FeignHttpMessageConverters
    // (auto-configured by spring-cloud-openfeign) collects every HttpMessageConverter bean
    // in the context, in place of the old Boot HttpMessageConverters wrapper.
    @Bean
    public HttpMessageConverter<?> feignJacksonHttpMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonHttpMessageConverter(jsonMapper);
    }

    @Bean
    @Primary
    public JsonMapper objectMapper(JsonMapper.Builder builder) {
        return builder
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .build();
    }

}
