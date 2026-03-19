package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User {
    private String id;
    private String forename;
    private String surname;
    private String email;
    private boolean active;
    private List<String> roles;

    @Override
    public String toString() {
        return email + " - " + forename + " " + surname;
    }

    public String toValueId() {
        return id + ":" + email;
    }
}
