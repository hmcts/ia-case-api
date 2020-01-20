package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

public class ProfessionalUsersResponse implements CaseData {

    @JsonAlias("users")
    private List<ProfessionalUser> professionalUsers;

    private ProfessionalUsersResponse() {
        //no op
    }

    public List<ProfessionalUser> getProfessionalUsers() {
        return professionalUsers;
    }
}
