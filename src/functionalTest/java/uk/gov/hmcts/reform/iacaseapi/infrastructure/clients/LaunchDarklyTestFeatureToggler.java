package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamUserDetails;

@Service
@Primary
@Slf4j
@Profile("functional")
public class LaunchDarklyTestFeatureToggler implements FeatureToggler {

    private final LDClientInterface ldClient;
    private final UserDetailsProvider userDetailsProvider;

    public LaunchDarklyTestFeatureToggler(LDClientInterface ldClient,
                                          UserDetailsProvider userDetailsProvider) {
        this.ldClient = ldClient;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean getValue(String key, Boolean defaultValue) {
        UserDetails userDetails;
        try {
            userDetails = userDetailsProvider.getUserDetails();
        } catch (Exception e) {
            log.error("Error retrieving user details for LaunchDarkly feature toggling", e);
            userDetails = new IdamUserDetails("token", "id", Collections.emptyList(),
                "emailAddress", "forename", "surname");
        }

        return ldClient.boolVariation(
            key,
            new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build(),
            defaultValue
        );
    }

    public LDValue getJsonValue(String key, LDValue defaultValue) {

        UserDetails userDetails = userDetailsProvider.getUserDetails();

        return ldClient.jsonValueVariation(
            key,
            new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build(),
            defaultValue
        );
    }

}
