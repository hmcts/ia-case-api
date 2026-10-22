package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    private String forename;
    private String surname;
    private String email;
    private boolean active;
    private List<String> roles;

    public String toRevokeAccessDlString(String nlrIdamId) {
        return email + " - " + forename + " " + surname + (this.id.equals(nlrIdamId) ? " (Non Legal Rep)" : " (Citizen)");
    }

    public String toValueId() {
        return id + ":" + email;
    }
}
