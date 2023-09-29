package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class DirectionPartiesResolverTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private DirectionPartiesResolver directionPartiesResolver;

    @BeforeEach
    public void setUp() {
        directionPartiesResolver = new DirectionPartiesResolver();
    }

    @ParameterizedTest
    @MethodSource("caseTypeScenarios")
    void should_return_parties_for_send_direction_events(YesOrNo appellantInDetention, Parties parties) {
        Parties expectedDirectionParties = Parties.BOTH;
        Map<Event, Parties> exampleInputOutputs =
            ImmutableMap
                .<Event, Parties>builder()
                .put(Event.LIST_CASE, parties)
                .put(Event.REQUEST_CASE_EDIT, parties)
                .put(Event.REQUEST_CASE_BUILDING, parties)
                .put(Event.FORCE_REQUEST_CASE_BUILDING, parties)
                .put(Event.REQUEST_RESPONSE_REVIEW, parties)
                .put(Event.REQUEST_NEW_HEARING_REQUIREMENTS, parties)
                .put(Event.SEND_DIRECTION, expectedDirectionParties)
                .put(Event.REQUEST_RESPONDENT_EVIDENCE, Parties.RESPONDENT)
                .put(Event.REQUEST_RESPONDENT_REVIEW, Parties.RESPONDENT)
                .put(Event.REQUEST_RESPONSE_AMEND, Parties.RESPONDENT)
                .put(Event.REQUEST_REASONS_FOR_APPEAL, Parties.APPELLANT)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final Event event = inputOutput.getKey();
                final Parties expectedParties = inputOutput.getValue();

                when(callback.getEvent()).thenReturn(event);

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(SEND_DIRECTION_PARTIES)).thenReturn(Optional.of(expectedDirectionParties));

                when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.ofNullable(appellantInDetention));
                when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.ofNullable(appellantInDetention));

                Parties actualDirectionParties = directionPartiesResolver.resolve(callback);

                assertEquals(expectedParties, actualDirectionParties);

                reset(callback);
            });
    }

    @Test
    void should_throw_when_callback_is_not_for_sending_a_direction() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);

        assertThatThrownBy(() -> directionPartiesResolver.resolve(callback))
            .hasMessage("Callback event is not for sending a direction")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> directionPartiesResolver.resolve(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> caseTypeScenarios() {
        return Stream.of(
                Arguments.of(YES, Parties.APPELLANT),
                Arguments.of(NO, Parties.LEGAL_REPRESENTATIVE)
        );
    }

}
