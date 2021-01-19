package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata;

import java.util.List;
import lombok.Value;

@Value
public class UserIds {
    List<String> userIds;
}
