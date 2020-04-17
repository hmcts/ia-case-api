package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration.LookupReferenceData;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeeService {

    private final FeesConfiguration feesConfiguration;
    private final FeesRegisterApi feesRegisterApi;

    public FeeDto getFee(FeeType feeType) throws FeignException {

        FeeResponse feeResponse = makeRequest(feeType);
        return FeeDto.builder()
            .calculatedAmount(feeResponse.getAmount())
            .description(feeResponse.getDescription())
            .version(feeResponse.getVersion())
            .code(feeResponse.getCode())
            .build();
    }

    private FeeResponse makeRequest(FeeType feeType) throws FeesRegisterException {

        try {
            LookupReferenceData lookupReferenceData = feesConfiguration.getFees().get(feeType.name());

            FeeResponse feeResponse = feesRegisterApi.findFee(
                lookupReferenceData.getChannel(),
                lookupReferenceData.getEvent(),
                lookupReferenceData.getJurisdiction1(),
                lookupReferenceData.getJurisdiction2(),
                lookupReferenceData.getKeyword(),
                lookupReferenceData.getService()
            );

            log.debug("Fee-register response {}", feeResponse);

            return feeResponse;
        } catch (FeignException ex) {
            log.error("Fee-register response error message: {}", ex);

            throw new FeesRegisterException("Error in retrieving fee from fees-register", ex);
        }
    }
}
