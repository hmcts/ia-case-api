package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false).genericModelSubstitutes(
            Optional.class).select().apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)).paths(
            PathSelectors.any()).build().securitySchemes(apiKeyList());
    }

    private List<ApiKey> apiKeyList() {
        return newArrayList(
            new ApiKey("Authorization", "Authorization", "header"),
            new ApiKey("ServiceAuthorization", "ServiceAuthorization", "header")
        );
    }
}
