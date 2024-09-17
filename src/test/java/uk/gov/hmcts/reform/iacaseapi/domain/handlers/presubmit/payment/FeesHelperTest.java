package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATED_DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment.FeesHelper.findFeeByHearingType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeeService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FeesHelperTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeeService feeService;

    private static final String FEE_WITH_HEARING_CODE = "FEE0123";
    private static final String FEE_WITH_HEARING_DESC = "Appeal determined with a hearing";
    private static final String FEE_WITHOUT_HEARING_CODE = "FEE0456";
    private static final String FEE_WITHOUT_HEARING_DESC = "Appeal determined without a hearing";
    private static final String VERSION = "1";

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @MethodSource("provideParameterValues")
    void should_return_correct_fee_and_save_proper_values(String decisionHearingFeeOption, String feeCode, String feeDesc, BigDecimal feeAmount) {
        when(asylumCase.read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(decisionHearingFeeOption));
        Fee feeMock = new Fee(feeCode, feeDesc, VERSION, feeAmount);
        when(feeService.getFee(any())).thenReturn(feeMock);

        final String amount = String.valueOf(new BigDecimal(feeMock.getAmountAsString()).multiply(new BigDecimal("100")));
        findFeeByHearingType(feeService, asylumCase);

        verify(asylumCase, times(1)).read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class);
        asylumCase.write(FEE_WITH_HEARING, feeMock.getCode());
        verify(asylumCase, times(1)).write(FEE_CODE, feeMock.getCode());
        verify(asylumCase, times(1)).write(FEE_DESCRIPTION, feeMock.getDescription());
        verify(asylumCase, times(1)).write(FEE_VERSION, feeMock.getVersion());
        verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, amount);
        verify(asylumCase, times(1)).write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);

        if ("decisionWithHearing".equals(decisionHearingFeeOption)) {
            verify(asylumCase, times(1)).write(FEE_WITH_HEARING, feeMock.getAmountAsString());
            verify(asylumCase, times(1)).write(PAYMENT_DESCRIPTION, FEE_WITH_HEARING_DESC);
            verify(asylumCase, times(1)).clear(FEE_WITHOUT_HEARING);
        } else if ("decisionWithoutHearing".equals(decisionHearingFeeOption)) {
            verify(asylumCase, times(1)).write(FEE_WITHOUT_HEARING, feeMock.getAmountAsString());
            verify(asylumCase, times(1)).write(PAYMENT_DESCRIPTION, FEE_WITHOUT_HEARING_DESC);
            verify(asylumCase, times(1)).clear(FEE_WITH_HEARING);
        }
    }

    @Test
    void should_return_null_if_fee_is_not_presented() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.empty());
        Assertions.assertNull(findFeeByHearingType(feeService, asylumCase));
    }

    private static Stream<Arguments> provideParameterValues() {
        return Stream.of(
            Arguments.of("decisionWithHearing", FEE_WITH_HEARING_CODE, FEE_WITH_HEARING_DESC, BigDecimal.valueOf(140.00)),
            Arguments.of("decisionWithoutHearing", FEE_WITHOUT_HEARING_CODE, FEE_WITHOUT_HEARING_DESC, BigDecimal.valueOf(80.00))
        );
    }
}