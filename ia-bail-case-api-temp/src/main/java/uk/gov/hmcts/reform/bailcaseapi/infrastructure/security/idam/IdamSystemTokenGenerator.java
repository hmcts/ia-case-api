package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.SystemTokenGenerator;

@Component
public class IdamSystemTokenGenerator implements SystemTokenGenerator {

    private final IdamService idamService;

    public IdamSystemTokenGenerator(IdamService idamService) {
        this.idamService = idamService;
    }

    @Override
    public String generate() {
        try {
            return idamService.getServiceUserToken();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get system user token from IDAM", ex);
        }
    }
}
