package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADJOURN_HEARING_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdjournWithoutDateHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    AsylumCase asylumCase;

    final AdjournWithoutDateHandler handler = new AdjournWithoutDateHandler();

    @ParameterizedTest
    @MethodSource("canHandleTestData")
    void it_can_handle_callback(Event event, PreSubmitCallbackStage callbackStage, boolean canBeHandledExpected) {
        when(callback.getEvent()).thenReturn(event);

        boolean actualResult = handler.canHandle(callbackStage, callback);

        assertThat(actualResult).isEqualTo(canBeHandledExpected);
    }

    private static Stream<Arguments> canHandleTestData() {

        List<Arguments> scenarios = new ArrayList<>();

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            for (Event event : Event.values()) {
                if (Event.ADJOURN_HEARING_WITHOUT_DATE.equals(event)
                    && PreSubmitCallbackStage.ABOUT_TO_SUBMIT.equals(callbackStage)) {
                    scenarios.add(Arguments.of(event, callbackStage, true));
                } else {
                    scenarios.add(Arguments.of(event, callbackStage, false));
                }
            }
        }

        return scenarios.stream();
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void sets_hearing_date_to_adjourned() {
        given(callback.getEvent()).willReturn(ADJOURN_HEARING_WITHOUT_DATE);
        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.class)).willReturn(Optional.of(State.PREPARE_FOR_HEARING));
        given(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).willReturn(Optional.of("05/05/2020"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE_ADJOURNED), eq("Adjourned"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE), eq("prepareForHearing"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.DATE_BEFORE_ADJOURN_WITHOUT_DATE), eq("05/05/2020"));
        then(asylumCase).should(times(1))
            .write(eq(AsylumCaseFieldDefinition.DOES_THE_CASE_NEED_TO_BE_RELISTED), eq(YesOrNo.NO));
    }
}