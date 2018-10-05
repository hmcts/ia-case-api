package uk.gov.hmcts.reform.iacaseapi.infrastructure.api;

import feign.Headers;
import feign.codec.Encoder;
import feign.form.FormData;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    configuration = DocumentConverterApi.MultipartSupportConfig.class,
    name = "docmosis-converter-api",
    url = "https://dws2.docmosis.com"
)
public interface DocumentConverterApi {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/services/rs/convert"
    )
    @Headers("Content-Type: multipart/form-data")
    Resource convert(
        @RequestParam("accessKey") String accessKey,
        @RequestParam("outputName") String outputName,
        @RequestParam("file") FormData file
    );

    class MultipartSupportConfig {

        @Autowired
        private ObjectFactory<HttpMessageConverters> messageConverters;

        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder(new SpringEncoder(messageConverters));
        }
    }
}
