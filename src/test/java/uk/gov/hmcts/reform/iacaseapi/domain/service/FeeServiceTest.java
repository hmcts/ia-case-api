package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeesConfiguration;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    private static final String FEE_WITH_HEARING_CODE = "FEE0123";
    private static final String FEE_WITH_HEARING_DESC = "Appeal determined with a hearing";
    private static final String FEE_WITHOUT_HEARING_CODE = "FEE0456";
    private static final String FEE_WITHOUT_HEARING_DESC = "Appeal determined without a hearing";
    private static final String VERSION = "1";
    private static final BigDecimal FEE_WITH_HEARING_AMOUNT = new BigDecimal("140.00");
    private static final BigDecimal FEE_WITHOUT_HEARING_AMOUNT = new BigDecimal("80.00");

    @Mock
    private FeesConfiguration feesConfiguration;
    @Mock private FeesRegisterApi feesRegisterApi;

    private FeeService feeService;

    @BeforeEach
    void setUp() {
        feeService = new FeeService(feesConfiguration, feesRegisterApi);
        when(feesConfiguration.getFees()).thenReturn(getFeeTypes());
    }

    @ParameterizedTest
    @MethodSource("provideFeeTypes")
    void should_return_correct_fee(FeeType feeType, String expectedCode, String expectedDescription, BigDecimal expectedAmount) {

        FeesConfiguration.LookupReferenceData lookupReferenceData = feesConfiguration
            .getFees()
            .get(feeType.getValue());

        when(feesRegisterApi.findFee(
            lookupReferenceData.getChannel(),
            lookupReferenceData.getEvent(),
            lookupReferenceData.getJurisdiction1(),
            lookupReferenceData.getJurisdiction2(),
            lookupReferenceData.getKeyword(),
            lookupReferenceData.getService()
        )).thenReturn(new FeeResponse(expectedCode, expectedDescription, VERSION, expectedAmount));

        Fee fee = feeService.getFee(feeType);

        assertThat(fee.getCode()).isEqualTo(expectedCode);
        assertThat(fee.getDescription()).isEqualTo(expectedDescription);
        assertThat(fee.getVersion()).isEqualTo(VERSION);
        assertThat(fee.getCalculatedAmount()).isEqualTo(expectedAmount);
    }

    @Test
    void should_throw_for_null_fee_type() {

        assertThatThrownBy(() -> feeService.getFee(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> provideFeeTypes() {
        return Stream.of(
            Arguments.of(FeeType.FEE_WITH_HEARING, FEE_WITH_HEARING_CODE, FEE_WITH_HEARING_DESC, FEE_WITH_HEARING_AMOUNT),
            Arguments.of(FeeType.FEE_WITHOUT_HEARING, FEE_WITHOUT_HEARING_CODE, FEE_WITHOUT_HEARING_DESC, FEE_WITHOUT_HEARING_AMOUNT)
        );
    }

    private Map<String, FeesConfiguration.LookupReferenceData> getFeeTypes() {

        final Map<String, FeesConfiguration.LookupReferenceData> feeTypeMap = new HashMap<>();

        FeesConfiguration.LookupReferenceData lookupReferenceData = getLookupReferenceData("ABC");
        feeTypeMap.put("feeWithHearing", lookupReferenceData);

        FeesConfiguration.LookupReferenceData lookupReferenceWithoutFeeData = getLookupReferenceData("DEF");
        feeTypeMap.put("feeWithoutHearing", lookupReferenceWithoutFeeData);

        return feeTypeMap;
    }

    @NotNull
    private static FeesConfiguration.LookupReferenceData getLookupReferenceData(String definition) {
        FeesConfiguration.LookupReferenceData lookupReferenceWithoutFeeData = new FeesConfiguration.LookupReferenceData();
        lookupReferenceWithoutFeeData.setChannel("default");
        lookupReferenceWithoutFeeData.setEvent("issue");
        lookupReferenceWithoutFeeData.setJurisdiction1("tribunal");
        lookupReferenceWithoutFeeData.setJurisdiction2("immigration and asylum chamber");
        lookupReferenceWithoutFeeData.setKeyword(definition);
        lookupReferenceWithoutFeeData.setService("other");
        return lookupReferenceWithoutFeeData;
    }
}