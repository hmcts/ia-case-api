package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration.LookupReferenceData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration;

@Service
@Slf4j
public class FeeService {

    private final FeesConfiguration feesConfiguration;
    private final FeesRegisterApi feesRegisterApi;

    public FeeService(
        FeesConfiguration feesConfiguration,
        FeesRegisterApi feesRegisterApi
    ) {
        this.feesConfiguration = feesConfiguration;
        this.feesRegisterApi = feesRegisterApi;
    }

    public Fee getFee(FeeType feeType) {

        FeeResponse feeResponse = makeRequest(feeType);
        log.debug("Fee-register response: {}", feeResponse);

        return new Fee(
            feeResponse.getCode(),
            feeResponse.getDescription(),
            feeResponse.getVersion(),
            feeResponse.getAmount());
    }

    private FeeResponse makeRequest(FeeType feeType) {

        LookupReferenceData lookupReferenceData = feesConfiguration.getFees().get(feeType.toString());

        return feesRegisterApi.findFee(
            lookupReferenceData.getChannel(),
            lookupReferenceData.getEvent(),
            lookupReferenceData.getJurisdiction1(),
            lookupReferenceData.getJurisdiction2(),
            lookupReferenceData.getKeyword(),
            lookupReferenceData.getService()
        );
    }
}
