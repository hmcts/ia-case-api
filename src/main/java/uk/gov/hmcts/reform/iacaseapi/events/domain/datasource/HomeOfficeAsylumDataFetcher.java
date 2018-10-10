package uk.gov.hmcts.reform.iacaseapi.events.domain.datasource;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.HomeOfficeAsylumData;

public interface HomeOfficeAsylumDataFetcher {

    Optional<HomeOfficeAsylumData> fetch(
        String homeOfficeReferenceNumber
    );
}
