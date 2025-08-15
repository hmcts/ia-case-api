package uk.gov.hmcts.reform.iacaseapi.testutils;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.iacaseapi.Application;

@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iacaseapi.infrastructure.clients",
    "uk.gov.hmcts.reform.iacaseapi.testutils.clients"
})
@SuppressWarnings("HideUtilityClassConstructor")
public class FunctionalSpringContext {

    public static void main(final String[] args) {

        new SpringApplicationBuilder(Application.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
