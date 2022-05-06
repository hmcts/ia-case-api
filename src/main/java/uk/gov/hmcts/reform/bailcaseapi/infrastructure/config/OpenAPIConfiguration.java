package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public GroupedOpenApi publicApi(OperationCustomizer customGlobalHeaders) {
        return GroupedOpenApi.builder()
            .group("case-event-handler-public")
            .pathsToMatch("/**")
            .addOperationCustomizer(customGlobalHeaders)
            .build();
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation customOperation, HandlerMethod handlerMethod) -> {
            Parameter serviceAuthorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name("ServiceAuthorization")
                .description("Keyword `Bearer` followed by a service-to-service token for a whitelisted micro-service")
                .required(true);
            customOperation.addParametersItem(serviceAuthorizationHeader);

            Parameter authorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name("Authorization")
                .description("")
                .required(true);
            customOperation.addParametersItem(authorizationHeader);

            return customOperation;
        };
    }

}
