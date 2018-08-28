package uk.gov.hmcts.reform.iacaseapi.domain.datasource;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeAsylumData;

public interface HomeOfficeAsylumDataFetcher {

    Optional<HomeOfficeAsylumData> fetch(
        String homeOfficeReferenceNumber
    );
}
