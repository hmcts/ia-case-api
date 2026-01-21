package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;

public interface UserDetails {

    String getAccessToken();

    String getId();

    List<String> getRoles();

    String getEmailAddress();

    String getForename();

    String getSurname();

    String getForenameAndSurname();
}
