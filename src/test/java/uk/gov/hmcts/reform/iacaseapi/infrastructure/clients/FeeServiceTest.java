package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"unchecked"})
public class FeeServiceTest {

    @Mock FeesConfiguration feesConfiguration;
    @Mock FeesRegisterApi feesRegisterApi;

    private FeeService feeService;

    @Before
    public void setUp() {

        feeService = new FeeService(feesConfiguration, feesRegisterApi);
    }

    @Test
    public void should_return_oral_hearing_fee() {

        when(feesConfiguration.getFees()).thenReturn(getFeeTypes());

        LookupReferenceData lookupReferenceData = feesConfiguration.getFees().get(FeeType.ORAL_FEE.toString());

        when(feesRegisterApi.findFee(
            lookupReferenceData.getChannel(),
            lookupReferenceData.getEvent(),
            lookupReferenceData.getJurisdiction1(),
            lookupReferenceData.getJurisdiction2(),
            lookupReferenceData.getKeyword(),
            lookupReferenceData.getService())).thenReturn(getFeeResponse());
        FeeDto feeDto = feeService.getFee(FeeType.ORAL_FEE);

        assertThat(feeDto.getCode()).isEqualTo("FEE0123");
        assertThat(feeDto.getDescription()).isEqualTo("description");
        assertThat(feeDto.getVersion()).isEqualTo(1);
        assertThat(feeDto.getCalculatedAmount()).isEqualTo(new BigDecimal("140.00"));

        verify(feesRegisterApi, times(1))
            .findFee(lookupReferenceData.getChannel(), lookupReferenceData.getEvent(),
                lookupReferenceData.getJurisdiction1(), lookupReferenceData.getJurisdiction2(),
                lookupReferenceData.getKeyword(), lookupReferenceData.getService());
    }

    @Test
    public void should_throw_when_fee_register_fails() {

        when(feesConfiguration.getFees()).thenReturn(getFeeTypes());

        assertThatThrownBy(() -> feeService.getFee(FeeType.ORAL_FEE))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    private Map<String, LookupReferenceData> getFeeTypes() {

        final Map<String, LookupReferenceData> feeTypeMap = new HashMap<>();

        LookupReferenceData lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setChannel("default");
        lookupReferenceData.setEvent("issue");
        lookupReferenceData.setJurisdiction1("tribunal");
        lookupReferenceData.setJurisdiction2("immigration and asylum chamber");
        lookupReferenceData.setKeyword("ABC");
        lookupReferenceData.setService("other");
        feeTypeMap.put(FeeType.ORAL_FEE.toString(), lookupReferenceData);

        return feeTypeMap;
    }

    private FeeResponse getFeeResponse() {

        FeeResponse feeResponse = new FeeResponse("FEE0123", "description", 1, new BigDecimal("140.00"));
        return feeResponse;
    }

}
