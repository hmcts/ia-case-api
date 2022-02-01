package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;


@ExtendWith(MockitoExtension.class)
public class CallbackTest {

    private final Event event = Event.START_APPLICATION;
    private final Optional<CaseDetails<CaseData>> caseDetailsBefore = Optional.empty();
    @Mock
    private CaseDetails<CaseData> caseDetails;
    private Callback<CaseData> callback;

    @BeforeEach
    public void setUp() {
        callback = new Callback<>(
            caseDetails,
            caseDetailsBefore,
            event
        );
    }

    @Test
    void should_hold_onto_values() {

        assertEquals(caseDetails, callback.getCaseDetails());
        assertEquals(caseDetailsBefore, callback.getCaseDetailsBefore());
        assertEquals(event, callback.getEvent());
    }

    @Test
    void should_not_allow_null_values_when_required_if_using_reflection() throws Exception {

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
    void should_not_allow_null_values_in_constructor() {

        assertThatThrownBy(() -> new Callback<>(null, caseDetailsBefore, event))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Callback<>(caseDetails, null, event))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Callback<>(caseDetails, caseDetailsBefore, null))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
