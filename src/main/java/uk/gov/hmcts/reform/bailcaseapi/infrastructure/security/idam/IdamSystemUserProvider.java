package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.SystemUserProvider;

@Component
public class IdamSystemUserProvider implements SystemUserProvider {

    private final IdamApi idamApi;

    public IdamSystemUserProvider(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    @Override
    public String getSystemUserId(String userToken) {

        try {

            return idamApi.userInfo(userToken).getUid();

        } catch (FeignException ex) {

            throw new IdentityManagerResponseException("Could not get system user id from IDAM", ex);
        }

    }
}
