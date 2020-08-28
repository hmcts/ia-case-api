package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import junitparams.converters.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamUserDetails;

@ExtendWith(MockitoExtension.class)
class RequestCaseBuildingHandlerTest {

    @Mock private
    static Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    UserDetailsProvider userDetailsProvider;
    @Mock private
    static AsylumCase asylumCase;
    @Mock private
    Appender<CaseNote> appender;
    @Mock private
    DateProvider dateProvider;

    @InjectMocks
    RequestCaseBuildingHandler requestCaseBuildingHandler;

    @Test
    void should_set_the_flag_on_valid_case_data() {
        mockCallback(asylumCase);

        requestCaseBuildingHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.NO);
    }

    void mockCallback(AsylumCase asylumCase) {
        when(callback.getEvent()).thenReturn(REQUEST_CASE_BUILDING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @MethodSource("generateAsylumCaseWithDifferentCaseNotesScenarios")
    void givenValidCallback_shouldCopyReasonToCaseNote(AsylumCase customAsylumCase,
                                                              IdValue<CaseNote> expectedIdCaseNote,
                                                              List<IdValue<CaseNote>> expectedAppendedCaseNotes) {
        mockCallback(customAsylumCase);
        mockUserDetailsProvider();

        given(dateProvider.now()).willReturn(LocalDate.now());

        given(appender.append(any(CaseNote.class), anyList())).willReturn(expectedAppendedCaseNotes);
        given(callback.getEvent()).willReturn(FORCE_REQUEST_CASE_BUILDING);

        PreSubmitCallbackResponse<AsylumCase> currentResult =
            requestCaseBuildingHandler.handle(ABOUT_TO_SUBMIT, callback);

        Optional<List<IdValue<CaseNote>>> currentCaseNotesOptional = currentResult.getData().read(CASE_NOTES);

        if (currentCaseNotesOptional.isPresent()) {
            List<IdValue<CaseNote>> caseNotes = currentCaseNotesOptional.get();
            assertThat(caseNotes).isNotEmpty();
            assertThat(caseNotes)
                .filteredOn(idValue -> idValue.getId().equals(expectedIdCaseNote.getId()))
                .containsOnlyOnce(expectedIdCaseNote);
        } else if (expectedIdCaseNote == null) {
            assertTrue(true, "No expected case note here");
        } else {
            fail("no case note present");
        }
        Optional<String> reason = currentResult.getData().read(REASON_TO_FORCE_REQUEST_CASE_BUILDING, String.class);
        assertThat(reason.isPresent()).isFalse();
    }

    private static Stream<Arguments> generateAsylumCaseWithDifferentCaseNotesScenarios() {
        String reasonToForceCase = "some reason";
        String otherReasonToForceCase = "some other reason";
        AsylumCase customAsylumCase = buildAsylumCaseWithNoCaseNotesAndReason(reasonToForceCase);
        AsylumCase customAsylumCaseWithSomeOtherReason = buildAsylumCaseWithNoCaseNotesAndReason(otherReasonToForceCase);

        AsylumCase customAsylumCaseWithExistingCaseNotes = buildAsylumCaseWithNoCaseNotesAndReason(reasonToForceCase);
        IdValue<CaseNote> idCaseNote1 = buildIdCaseNote(reasonToForceCase, "1");
        IdValue<CaseNote> idCaseNote2 = buildIdCaseNote(otherReasonToForceCase, "2");
        List<IdValue<CaseNote>> caseNotes = Arrays.asList(idCaseNote1, idCaseNote2);
        customAsylumCaseWithExistingCaseNotes.write(CASE_NOTES, caseNotes);

        return Stream.of(
            Arguments.of(new AsylumCase(), null, null),
            Arguments.of(customAsylumCase, buildIdCaseNote(reasonToForceCase, "1"), Collections.singletonList(buildIdCaseNote(reasonToForceCase, "1"))),
            Arguments.of(customAsylumCaseWithSomeOtherReason, buildIdCaseNote(otherReasonToForceCase, "1"), Collections.singletonList(buildIdCaseNote(otherReasonToForceCase, "1"))),
            Arguments.of(customAsylumCaseWithExistingCaseNotes, buildIdCaseNote(reasonToForceCase, "3"), Arrays.asList(idCaseNote1, idCaseNote2, buildIdCaseNote(reasonToForceCase, "3")))
        );
    }

    private static CaseNote buildCaseNote(String reason) {
        return new CaseNote("Force case from Awaiting Respondent Evidence to Case Building",
            reason, "some forename some surname", LocalDate.now().toString());
    }

    private static AsylumCase buildAsylumCaseWithNoCaseNotesAndReason(String reasonToForceCase) {
        AsylumCase customAsylumCase = new AsylumCase();
        customAsylumCase.write(REASON_TO_FORCE_REQUEST_CASE_BUILDING, reasonToForceCase);
        return customAsylumCase;
    }

    void mockUserDetailsProvider() {
        given(userDetailsProvider.getUserDetails())
            .willReturn(new IdamUserDetails("some token", "some id", Collections.emptyList(),
                "some email", "some forename", "some surname"));
    }

    private  static  IdValue<CaseNote> buildIdCaseNote(String noteDescription, String id) {
        return new IdValue<>(id, buildCaseNote(noteDescription));
    }

    @ParameterizedTest
    @MethodSource("generateExceptionScenarios")
    void handling_should_throw_if_cannot_actually_handle(PreSubmitCallbackStage callbackStage,
                                                                Callback<AsylumCase> callback,
                                                                String expectedMsg) {

        assertThatThrownBy(() -> requestCaseBuildingHandler.handle(callbackStage, callback))
            .hasMessage(expectedMsg)
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.NO);
    }

    private static Stream<Arguments> generateExceptionScenarios() {
        CaseDetails<AsylumCase> dummyCaseDetails = new CaseDetails<>(1L, "", State.APPEAL_STARTED,
            asylumCase, LocalDateTime.now());
        Callback<AsylumCase> dummyCallback = new Callback<>(dummyCaseDetails, Optional.empty(), SEND_DIRECTION);

        return Stream.of(
            Arguments.of(ABOUT_TO_START, dummyCallback, "Cannot handle callback"),
            Arguments.of(ABOUT_TO_SUBMIT, dummyCallback, "Cannot handle callback")
        );
    }

    @ParameterizedTest
    @MethodSource("canHandleTestData")
    void canHandle(PreSubmitCallbackStage callbackStage, Event event, boolean expectedResult) {
        if (!callbackStage.equals(PreSubmitCallbackStage.MID_EVENT)) {
            given(callback.getEvent()).willReturn(event);
        }

        boolean actualResult = requestCaseBuildingHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> canHandleTestData() {
        return Stream.of(
            Arguments.of(ABOUT_TO_SUBMIT, REQUEST_CASE_BUILDING, true),
            Arguments.of(ABOUT_TO_SUBMIT, FORCE_REQUEST_CASE_BUILDING, true),
            Arguments.of(MID_EVENT, REQUEST_CASE_BUILDING, false),
            Arguments.of(ABOUT_TO_SUBMIT, UPLOAD_HEARING_RECORDING, false)
        );
    }

    @ParameterizedTest
    @MethodSource("generateNullArgsScenarios")
    void given_null_callback_should_throw_exception(@Nullable PreSubmitCallbackStage callbackStage,
                                                           @Nullable Callback<AsylumCase> callback,
                                                           String expectedMessage) {

        assertThatThrownBy(() -> requestCaseBuildingHandler.canHandle(callbackStage, callback))
            .hasMessage(expectedMessage)
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> generateNullArgsScenarios() {

        return Stream.of(
            Arguments.of(null, callback, "callbackStage must not be null"),
            Arguments.of(ABOUT_TO_SUBMIT, null, "callback must not be null")
        );
    }

}
