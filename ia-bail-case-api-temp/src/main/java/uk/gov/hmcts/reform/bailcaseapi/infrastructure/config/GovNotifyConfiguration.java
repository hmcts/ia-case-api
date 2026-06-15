package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Slf4j
@Configuration
public class GovNotifyConfiguration {
    @Bean("BailClient")
    public NotificationClient notificationBailClient(
            @Value("${govnotify.bail.key}") String key,
            @Value("${govnotify.baseUrl}") String govNotifyBaseUrl
    ) {
        requireNonNull(key);

        return new NotificationClient(key, govNotifyBaseUrl);
    }
}
