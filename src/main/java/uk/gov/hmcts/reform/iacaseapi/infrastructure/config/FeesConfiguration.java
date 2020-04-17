package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@EnableConfigurationProperties
@ConfigurationProperties("fees-register")
public class FeesConfiguration {

    private final Map<String, LookupReferenceData> fees = new HashMap<>();

    @Getter
    @Setter
    public static class LookupReferenceData {
        private String channel;
        private String event;
        private String jurisdiction1;
        private String jurisdiction2;
        private String keyword;
        private String service;


    }
}
