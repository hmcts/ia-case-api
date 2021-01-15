package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment {

    private final String id;
    private final LocalDateTime created;
    private final List<String> authorisations;
    private final ActorIdType actorIdType;
    private final String actorId;
    private final RoleType roleType;
    private final RoleName roleName;
    private final RoleCategory roleCategory;
    private final Classification classification;
    private final GrantType grantType;
    private final Boolean readOnly;
    private final Map<Attributes, String> attributes;

    @JsonCreator

    public Assignment(@JsonProperty("id") String id,
                      @JsonProperty("created") LocalDateTime created,
                      @JsonProperty("authorisations") List<String> authorisations,
                      @JsonProperty("actorIdType") ActorIdType actorIdType,
                      @JsonProperty("actorId") String actorId,
                      @JsonProperty("roleType") RoleType roleType,
                      @JsonProperty("roleName") RoleName roleName,
                      @JsonProperty("roleCategory") RoleCategory roleCategory,
                      @JsonProperty("classification") Classification classification,
                      @JsonProperty("grantType") GrantType grantType,
                      @JsonProperty("readOnly") Boolean readOnly,
                      @JsonProperty("attributes") Map<Attributes, String> attributes) {
        this.id = id;
        this.created = created;
        this.authorisations = authorisations;
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

    public String getId() {
        return id;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public List<String> getAuthorisations() {
        return authorisations;
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

    public RoleName getRoleName() {
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

    public Map<Attributes, String> getAttributes() {
        return attributes;
    }
}
