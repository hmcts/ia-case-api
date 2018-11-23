package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.RequiredFieldMissingException;

@RunWith(MockitoJUnitRunner.class)
public class CallbackTest {

    private final Event event = Event.START_APPEAL;
    @Mock private CaseDetails<CaseData> caseDetails;
    private final Optional<CaseDetails<CaseData>> caseDetailsBefore = Optional.empty();

    private Callback<CaseData> callback;

    @Before
    public void setUp() {
        callback = new Callback<>(
            caseDetails,
            caseDetailsBefore,
            event
        );
    }

    @Test
    public void should_hold_onto_values() {

        assertEquals(caseDetails, callback.getCaseDetails());
        assertEquals(caseDetailsBefore, callback.getCaseDetailsBefore());
        assertEquals(event, callback.getEvent());
    }

    @Test
    public void should_not_allow_null_values_when_required_if_using_reflection() throws Exception {

        Class<?> clazz = Class.forName(Callback.class.getName());
        Optional<Constructor<?>> constructorOpt = Stream.of(clazz.getDeclaredConstructors())
            .filter(constructor -> constructor.getParameterCount() == 0).findFirst();

        assertTrue(constructorOpt.isPresent());

        constructorOpt.get().setAccessible(true);
        Callback refCallback = (Callback) constructorOpt.get().newInstance();

        assertThatThrownBy(refCallback::getCaseDetails)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("caseDetails");

        assertEquals(null, refCallback.getEvent());
        assertEquals(Optional.empty(), refCallback.getCaseDetailsBefore());
    }

    @Test
    public void should_not_allow_null_values_in_constructor() {

        assertThatThrownBy(() -> new Callback<>(null, caseDetailsBefore, event))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Callback<>(caseDetails, null, event))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Callback<>(caseDetails, caseDetailsBefore, null))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
