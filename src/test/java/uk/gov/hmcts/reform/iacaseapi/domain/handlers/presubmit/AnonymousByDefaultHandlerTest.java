package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ANONYMITY_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.CaseFlagDto;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.Flag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.FlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseFlagMapper;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.RdCommonDataClient;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnonymousByDefaultHandlerTest {

    private static final String FLAG_CODE = "CF0012";
    private static final String FLAG_NAME = "RRO (Restricted Reporting Order / Anonymisation)";
    private static final String PATH = "Case";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RdCommonDataClient rdCommonDataClient;
    @Captor
    private ArgumentCaptor<StrategicCaseFlag> asylumValueCaptor;
    private AnonymousByDefaultHandler anonymousByDefaultHandler;

    @BeforeEach
    public void setUp() {
        anonymousByDefaultHandler = new AnonymousByDefaultHandler(rdCommonDataClient);
    }

    @ParameterizedTest
    @CsvSource({"SUBMIT_APPEAL,RP"})//, "SUBMIT_APPEAL,PA", "PAY_AND_SUBMIT_APPEAL,RP", "PAY_AND_SUBMIT_APPEAL,PA"
    void should_set_anonymity_flag_for_PA_appeal(Event event, AppealType appealType) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of("John Doe"));

        Flag flag = Flag.builder().flagDetails(getFlagDetail()).build();
        CaseFlagDto caseFlagDto = CaseFlagDto.builder().flag(flag).build();

        when(rdCommonDataClient.getStrategicCaseFlags()).thenReturn(caseFlagDto);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(APPELLANT_NAME_FOR_DISPLAY, String.class);
        verify(asylumCase, times(1)).write(eq(CASE_LEVEL_FLAGS), asylumValueCaptor.capture());
        verify(asylumCase, times(1)).write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);

        assertNotNull(asylumValueCaptor.capture(), "Flag not found");

        CaseFlagDetail anonymityCaseFlagDetail = asylumValueCaptor.getValue().getDetails().get(0);
        assertEquals(anonymityCaseFlagDetail.getCaseFlagValue().getFlagCode(), FLAG_CODE,
                "Flag Code should match");
        assertEquals(anonymityCaseFlagDetail.getCaseFlagValue().getCaseFlagPath().get(0).getValue(), PATH,
                "Path should be case");
        assertEquals(anonymityCaseFlagDetail.getCaseFlagValue().getName(),
                FLAG_NAME, "Name should match");
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "PAY_AND_SUBMIT_APPEAL"})
    void anonymity_flag_should_not_be_set_for_non_PA_or_RP_appeal_if_not_already_set(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = anonymousByDefaultHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (callback.getEvent() == SUBMIT_APPEAL
                        || callback.getEvent() == PAY_AND_SUBMIT_APPEAL)) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> anonymousByDefaultHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> anonymousByDefaultHandler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

    }

    private List<FlagDetail> getFlagDetail() {
        List<FlagDetail> childFlagDetails = new ArrayList<>();

        FlagDetail childFlagDetail = FlagDetail.builder()
            .name(FLAG_NAME)
            .flagCode(FLAG_CODE)
            .hearingRelevant(Boolean.TRUE)
            .path(PATH)
            .build();
        childFlagDetails.add(childFlagDetail);

        List<FlagDetail> flagDetails = new ArrayList<>();
        FlagDetail flagDetail = FlagDetail.builder()
            .name(PATH)
            .childFlags(childFlagDetails)
            .build();
        flagDetails.add(flagDetail);
        return flagDetails;
    }
}
