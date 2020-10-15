package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RequestedRoles {
    private ActorIdType actorIdType;
    private String actorId;
    private RoleType roleType;
    private String roleName;
    private RoleCategory roleCategory;
    private Classification classification;
    private GrantType grantType;
    private Boolean readOnly;
    private Map<String, String> attributes;

    public RequestedRoles(ActorIdType actorIdType,
                          String actorId,
                          RoleType roleType,
                          String roleName,
                          RoleCategory roleCategory,
                          Classification classification,
                          GrantType grantType,
                          Boolean readOnly,
                          Map<String, String> attributes) {
        this.actorIdType = actorIdType;
        this.actorId = actorId;
        this.roleType = roleType;
        this.roleName = roleName;
        this.roleCategory = roleCategory;
        this.classification = classification;
        this.grantType = grantType;
        this.readOnly = readOnly;
        this.attributes = attributes;
    }

    public ActorIdType getActorIdType() {
        return actorIdType;
    }

    public String getActorId() {
        return actorId;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getRoleName() {
        return roleName;
    }

    public RoleCategory getRoleCategory() {
        return roleCategory;
    }

    public Classification getClassification() {
        return classification;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
