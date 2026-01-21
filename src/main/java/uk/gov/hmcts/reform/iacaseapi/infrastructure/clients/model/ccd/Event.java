package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Event {

    private String id;
    private String summary;
    private String description;
}
