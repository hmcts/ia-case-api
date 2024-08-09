package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.service.notify.NotificationClient;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
public class GovNotifyConfiguration {

    @Bean
    @Primary
    public NotificationClient notificationClient(
        @Value("${govnotify.key}") String key,
        @Value("${govnotify.baseUrl}") String govNotifyBaseUrl
    ) {
        requireNonNull(key);

        return new NotificationClient(key, govNotifyBaseUrl);
    }

    @Bean("BailClient")
    public NotificationClient notificationBailClient(
            @Value("${govnotify.bail.key}") String key,
            @Value("${govnotify.baseUrl}") String govNotifyBaseUrl
    ) {
        requireNonNull(key);

        return new NotificationClient(key, govNotifyBaseUrl);
    }
}
