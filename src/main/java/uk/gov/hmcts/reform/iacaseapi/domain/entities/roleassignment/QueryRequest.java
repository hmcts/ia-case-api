package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@JsonNaming
@JsonInclude(Include.NON_NULL)
@ToString
public final class QueryRequest {

    private final List<String> actorId;
    private final List<RoleType> roleType;
    private final List<RoleName> roleName;
    private final List<Classification> classification;
    private final List<GrantType> grantType;
    private final LocalDateTime validAt;
    private final List<RoleCategory> roleCategory;
    private final Map<Attributes, List<String>> attributes;
    private final List<String> authorisations;

    public List<String> getActorId() {
        return actorId;
    }

    public List<RoleType> getRoleType() {
        return roleType;
    }

    public List<RoleName> getRoleName() {
        return roleName;
    }

    public List<Classification> getClassification() {
        return classification;
    }

    public List<GrantType> getGrantType() {
        return grantType;
    }

    public LocalDateTime getValidAt() {
        return validAt;
    }

    public List<RoleCategory> getRoleCategory() {
        return roleCategory;
    }

    public Map<Attributes, List<String>> getAttributes() {
        return attributes;
    }

    public List<String> getAuthorisations() {
        return authorisations;
    }
}
