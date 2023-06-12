package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CustodialSentenceDate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CustodialDateValidatorTest {

    private static final String CALLBACK_ERROR_MESSAGE_LR = "Client's release date must be in the future";
    private static final String CALLBACK_ERROR_MESSAGE_AO = "Appellant's release date must be in the future";
    private static final String CUSTODIAL_SENTENCE_PAGE_ID = "custodialSentence";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustodialSentenceDate custodialSentenceDate;
    private LocalDate now;
    private CustodialDateValidator custodialDateValidator;

    @BeforeEach
    public void setUp() {
        custodialDateValidator = new CustodialDateValidator();
        now = LocalDate.now();

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(CUSTODIAL_SENTENCE_PAGE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = { CUSTODIAL_SENTENCE_PAGE_ID, ""})
    void it_can_handle_callback(String pageId) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(pageId);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = custodialDateValidator.canHandle(callbackStage, callback);

                if (Arrays.asList(Event.START_APPEAL,
                    Event.EDIT_APPEAL,
                    Event.EDIT_APPEAL_AFTER_SUBMIT,
                    Event.MARK_APPEAL_AS_DETAINED,
                    Event.UPDATE_DETENTION_LOCATION).contains(event)
                    && callbackStage == MID_EVENT
                    && callback.getPageId().equals(CUSTODIAL_SENTENCE_PAGE_ID)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> custodialDateValidator.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> custodialDateValidator.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> custodialDateValidator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> custodialDateValidator.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL",
        "EDIT_APPEAL",
        "EDIT_APPEAL_AFTER_SUBMIT",
        "MARK_APPEAL_AS_DETAINED",
        "UPDATE_DETENTION_LOCATION"
    })
    void should_error_when_date_is_not_future(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(CUSTODIAL_SENTENCE, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(DATE_CUSTODIAL_SENTENCE, CustodialSentenceDate.class))
            .thenReturn(Optional.of(custodialSentenceDate));
        when(asylumCase.read(DATE_CUSTODIAL_SENTENCE_AO, CustodialSentenceDate.class))
            .thenReturn(Optional.of(custodialSentenceDate));

        String yesterday = now.minusDays(1).toString();
        when(custodialSentenceDate.getCustodialDate()).thenReturn(yesterday);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            custodialDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();

        if (event.equals(Event.MARK_APPEAL_AS_DETAINED)) {
            assertThat(errors).hasSize(1).containsOnly(CALLBACK_ERROR_MESSAGE_AO);
        } else {
            assertThat(errors).hasSize(1).containsOnly(CALLBACK_ERROR_MESSAGE_LR);
        }
    }

    @Test
    void should_not_error_if_date_is_null() {
        when(asylumCase.read(DATE_CUSTODIAL_SENTENCE, CustodialSentenceDate.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            custodialDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }
}

