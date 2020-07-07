package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfessionalUsersResponse implements CaseData {

    private List<ProfessionalUser> users;

    private ProfessionalUsersResponse() {
    }

    public ProfessionalUsersResponse(List<ProfessionalUser> users) {
        this.users = users;
    }

    public List<ProfessionalUser> getUsers() {
        return users;
    }
}
