package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Service
@Primary
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

        UserDetails userDetails = userDetailsProvider.getUserDetails();

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
