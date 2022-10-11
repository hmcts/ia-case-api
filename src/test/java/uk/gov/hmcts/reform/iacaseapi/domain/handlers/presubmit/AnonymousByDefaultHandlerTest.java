package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
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

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RdCommonDataClient rdCommonDataClient;
    @Mock
    private StrategicCaseFlag strategicCaseFlag;
    @Mock
    private CaseFlagMapper caseFlagMapper;

    private CaseFlagDto caseFlagDto;


    private AnonymousByDefaultHandler anonymousByDefaultHandler;

    @BeforeEach
    public void setUp() {
        anonymousByDefaultHandler = new AnonymousByDefaultHandler(rdCommonDataClient, caseFlagMapper);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL"}) //, "PAY_AND_SUBMIT_APPEAL"
    void should_set_anonymity_flag_for_PA_appeal(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY)).thenReturn(Optional.of("John Doe"));

        var caseFlagDto = new CaseFlagDto();
        var flag = new Flag();
        flag.setFlagDetails(getFlagDetail());
        List<Flag> flags = new ArrayList<>();
        flags.add(flag);

        caseFlagDto.setFlags(flags);

        when(rdCommonDataClient.getStrategicCaseFlags()).thenReturn(caseFlagDto);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        //when(caseFlagMapper.buildStrategicCaseFlagDetail(caseFlagDto.getFlags().get(0),
        //    StrategicCaseFlagType.RRO_ANONYMISATION, "Case", "John Doe"))
        //    .thenReturn(strategicCaseFlag);

        asylumCase.write(APPELLANT_NAME_FOR_DISPLAY, "John Doe");

        verify(asylumCase, times(1)).write(CASE_LEVEL_FLAGS, strategicCaseFlag);
        verify(asylumCase, times(1)).write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "PAY_AND_SUBMIT_APPEAL"})
    void should_set_anonymity_flag_for_RP_appeal(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));
        when(asylumCase.read(CASE_FLAGS)).thenReturn(Optional.of(Collections.emptyList()));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(CASE_LEVEL_FLAGS);
        verify(asylumCase, times(1)).write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
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
}
