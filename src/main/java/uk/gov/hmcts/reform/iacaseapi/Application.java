package uk.gov.hmcts.reform.iacaseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableFeignClients(basePackages =
    {
    "uk.gov.hmcts.reform.auth",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iacaseapi",
    "uk.gov.hmcts.reform.ccd"
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
