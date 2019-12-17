package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

@JsonRootName("users")
public class ProfessionalUsersResponse implements CaseData {

    private List<ProfessionalUser> professionalUsers;

    @JsonCreator
    public ProfessionalUsersResponse(List<ProfessionalUser> professionalUsers) {
        this.professionalUsers = professionalUsers;
    }

    public List<ProfessionalUser> getProfessionalUsers() {
        return professionalUsers;
    }
}
