package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RequestedRoles {
    private final ActorIdType actorIdType;
    private final String actorId;
    private final RoleType roleType;
    private final String roleName;
    private final RoleCategory roleCategory;
    private final Classification classification;
    private final GrantType grantType;
    private final Boolean readOnly;
    private final Map<String, String> attributes;

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
