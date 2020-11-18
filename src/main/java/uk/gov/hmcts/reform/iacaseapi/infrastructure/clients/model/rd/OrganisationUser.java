package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.rd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationUser {
    private String userIdentifier;
}
