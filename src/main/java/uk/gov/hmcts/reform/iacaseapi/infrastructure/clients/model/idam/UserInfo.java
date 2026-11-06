package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @JsonProperty("sub")
    private String email;
    private String uid;
    @Setter
    private List<String> roles;
    private String name;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
}
