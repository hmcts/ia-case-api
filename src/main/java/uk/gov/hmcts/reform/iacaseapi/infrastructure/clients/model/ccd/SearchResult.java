package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
    int total;
    List<CaseDetails> cases;
}
