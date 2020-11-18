package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.rd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationUsers {
    private List<OrganisationUser> users;
}
