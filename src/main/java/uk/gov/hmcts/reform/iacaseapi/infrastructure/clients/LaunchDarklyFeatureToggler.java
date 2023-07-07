package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {

    private final LDClientInterface ldClient;
    private final UserDetailsProvider userDetailsProvider;

    public LaunchDarklyFeatureToggler(LDClientInterface ldClient,
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

}
