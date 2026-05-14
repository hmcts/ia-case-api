package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@Service
@Primary
@Profile("functional")
public class LaunchDarklyTestFeatureToggler implements FeatureToggler {

    private final LDClientInterface ldClient;
    private final UserDetailsProvider userDetailsProvider;
    @Autowired
    private final IdamService idamService;

    public LaunchDarklyTestFeatureToggler(LDClientInterface ldClient,
                                          UserDetailsProvider userDetailsProvider,
                                          IdamService idamService) {
        this.ldClient = ldClient;
        this.userDetailsProvider = userDetailsProvider;
        this.idamService = idamService;
    }

    public boolean getValue(String key, Boolean defaultValue) {

        UserDetails userDetails = userDetailsProvider.getUserDetails();
        LDContext ldContext = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();
        return ldClient.boolVariation(key, ldContext, defaultValue);
    }

    public LDValue getJsonValue(String key, LDValue defaultValue) {

        UserDetails userDetails = userDetailsProvider.getUserDetails();
        LDContext ldContext = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();
        return ldClient.jsonValueVariation(key, ldContext, defaultValue);
    }

}
