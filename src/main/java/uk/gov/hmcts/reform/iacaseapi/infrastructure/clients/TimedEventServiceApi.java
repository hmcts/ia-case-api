package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import feign.Feign;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
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

        // feignHttpMessageConverters already includes the JacksonJsonHttpMessageConverter
        // bean declared in the global FeignConfiguration (built from the shared, customized
        // JsonMapper), so no need to construct one here.
        @Bean
        public Decoder decoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
            return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverters));
        }

        @Bean
        @Scope("prototype")
        public Feign.Builder feignBuilder() {
            return Feign.builder();
        }

    }

}
