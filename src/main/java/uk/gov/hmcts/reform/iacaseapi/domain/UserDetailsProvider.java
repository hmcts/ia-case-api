package uk.gov.hmcts.reform.iacaseapi.domain;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

public interface UserDetailsProvider {

    UserDetails getUserDetails();

    UserDetails getUserDetails(String authenticationHeader);

}
