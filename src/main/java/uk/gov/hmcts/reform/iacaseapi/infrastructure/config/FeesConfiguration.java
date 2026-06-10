package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("fees-register")
public class FeesConfiguration {

    private final Map<String, LookupReferenceData> fees = new HashMap<>();

    public Map<String, LookupReferenceData> getFees() {
        return fees;
    }

    public static class LookupReferenceData {
        private String channel;
        private String event;
        private String jurisdiction1;
        private String jurisdiction2;
        private String keyword;
        private String service;

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getJurisdiction1() {
            return jurisdiction1;
        }

        public void setJurisdiction1(String jurisdiction1) {
            this.jurisdiction1 = jurisdiction1;
        }

        public String getJurisdiction2() {
            return jurisdiction2;
        }

        public void setJurisdiction2(String jurisdiction2) {
            this.jurisdiction2 = jurisdiction2;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }
    }
}
