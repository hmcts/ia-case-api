package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.launchdarkly.sdk.LDUser;
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
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;

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
        LDUser user;
        try {
            UserDetails userDetails = userDetailsProvider.getUserDetails();
            user = new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build();
        } catch (Exception e) {
            // Fallback to idam system user if user details cannot be retrieved
            String token = idamService.getServiceUserToken();
            UserInfo userDetails = idamService.getUserInfo(token);
            user = new LDUser.Builder(userDetails.getUid())
                .firstName(userDetails.getGivenName())
                .lastName(userDetails.getFamilyName())
                .email(userDetails.getEmail())
                .build();
        }
        return ldClient.boolVariation(
            key, user, defaultValue
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
