package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
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
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;

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
        this.startTime = null;
        this.endTime = null;
    }
}
