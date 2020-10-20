package uk.gov.hmcts.reform.iacaseapi.util;

import com.launchdarkly.client.LDClientInterface;
import com.launchdarkly.client.LDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamUserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Component
public class LaunchDarklyFunctionalTestClient {

    @Autowired private IdamApi idamApi;
    @Autowired private LDClientInterface ldClient;

    public boolean getKey(String key, String accessToken) {

        UserDetails userDetails = getUserDetails(accessToken);

        LDUser ldUser =  new LDUser.Builder(userDetails.getId())
            .firstName(userDetails.getForename())
            .lastName(userDetails.getSurname())
            .email(userDetails.getEmailAddress())
            .build();

        return ldClient.boolVariation(key, ldUser, false);
    }

    private IdamUserDetails getUserDetails(String accessToken) {
        try {

            UserInfo userInfo = idamApi.userInfo(accessToken);

            return new IdamUserDetails(
                accessToken,
                userInfo.getUid(),
                userInfo.getRoles(),
                userInfo.getEmail(),
                userInfo.getGivenName(),
                userInfo.getFamilyName()
            );

        } catch (RestClientResponseException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }
    }
}
