package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.DisableHystrixFeignConfiguration;

@FeignClient(
    name = "timed-event-service-api",
    url = "${timed-event-service.url}",
    configuration = {TimedEventServiceApi.Configuration.class, DisableHystrixFeignConfiguration.class}
)
public interface TimedEventServiceApi {

    @PostMapping(value = "/timed-event", produces = "application/json", consumes = "application/json")
    TimedEvent submitTimedEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @RequestBody TimedEvent content
    );

    @DeleteMapping(value = "/timed-event/{id}", produces = "application/json", consumes = "application/json")
    void deleteTimedEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("id") String jobKey
    );


    class Configuration {

        @Bean
        public Decoder decoder(ObjectMapper objectMapper) {
            HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
            return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(jacksonConverter), new EmptyObjectProvider<>()));
        }

        @Bean
        @Scope("prototype")
        public Feign.Builder feignBuilder() {
            return Feign.builder();
        }

    }



}

