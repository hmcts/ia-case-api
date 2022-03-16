package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfessionalUser {

    public ProfessionalUser() {
        //no op
    }

    public ProfessionalUser(String userIdentifier, String firstName, String lastName, String email,
                            List<String> roles, String idamStatus, String idamStatusCode, String idamMessage) {
        this.userIdentifier = userIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.idamStatus = idamStatus;
        this.idamStatusCode = idamStatusCode;
        this.idamMessage = idamMessage;
    }

    private String userIdentifier;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private String idamStatus;
    private String idamStatusCode;
    private String idamMessage;

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getIdamStatus() {
        return idamStatus;
    }

    public String getIdamStatusCode() {
        return idamStatusCode;
    }

    public String getIdamMessage() {
        return idamMessage;
    }
}

