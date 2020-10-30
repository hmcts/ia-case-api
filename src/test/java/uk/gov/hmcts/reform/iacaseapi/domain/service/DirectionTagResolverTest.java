package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DirectionTagResolverTest {

    @Mock private Callback<AsylumCase> callback;

    private DirectionTagResolver directionTagResolver;

    @Before
    public void setUp() {
        directionTagResolver = new DirectionTagResolver();
    }

    @Test
    public void should_return_parties_for_send_direction_events() {

        Map<Event, DirectionTag> exampleInputOutputs =
            ImmutableMap
                .<Event, DirectionTag>builder()
                .put(Event.SEND_DIRECTION, DirectionTag.NONE)
                .put(Event.REQUEST_CASE_EDIT, DirectionTag.CASE_EDIT)
                .put(Event.REQUEST_RESPONDENT_EVIDENCE, DirectionTag.RESPONDENT_EVIDENCE)
                .put(Event.REQUEST_RESPONDENT_REVIEW, DirectionTag.RESPONDENT_REVIEW)
                .put(Event.REQUEST_CASE_BUILDING, DirectionTag.REQUEST_CASE_BUILDING)
                .put(Event.REQUEST_RESPONSE_REVIEW, DirectionTag.REQUEST_RESPONSE_REVIEW)
                .put(Event.REQUEST_RESPONSE_AMEND, DirectionTag.REQUEST_RESPONSE_AMEND)
                .put(Event.REQUEST_NEW_HEARING_REQUIREMENTS, DirectionTag.REQUEST_NEW_HEARING_REQUIREMENTS)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final Event event = inputOutput.getKey();
                final DirectionTag expectedDirectionTag = inputOutput.getValue();

                DirectionTag actualDirectionTag = directionTagResolver.resolve(event);

                assertEquals(expectedDirectionTag, actualDirectionTag);

                reset(callback);
            });
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> directionTagResolver.resolve(null))
            .hasMessage("event must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
