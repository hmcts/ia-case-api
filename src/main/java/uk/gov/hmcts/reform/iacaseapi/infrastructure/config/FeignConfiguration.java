package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class FeignConfiguration {

    @Bean
    @Primary
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .build();
    }

    @Bean
    public HttpMessageConverter<?> feignJacksonHttpMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonHttpMessageConverter(jsonMapper);
    }

    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
        ObjectProvider<ClientHttpMessageConvertersCustomizer> messageConverters,
        ObjectProvider<HttpMessageConverterCustomizer> customizers
    ) {
        return new FeignHttpMessageConverters(messageConverters, customizers);
    }

    @Bean
    @Primary
    public Encoder feignEncoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
        return new SpringEncoder(feignHttpMessageConverters);
    }

    @Bean(name = "multipartFormEncoder")
    public Encoder multipartFormEncoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
        return new SpringFormEncoder(new SpringEncoder(feignHttpMessageConverters));
    }

    @Bean
    @Primary
    public Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverters));
    }
}
