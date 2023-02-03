package uk.gov.hmcts.reform.iacaseapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import static org.springframework.boot.SpringApplication.*;

@SpringBootApplication
@EnableRetry
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        run(Application.class, args);
    }
}
