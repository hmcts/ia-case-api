package uk.gov.hmcts.reform.iacaseapi.utils;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

public class TestUtils {
    private static Stream<Arguments> eventAndCallbackStages() {
        return Stream.of(
            Event.values()
        ).flatMap(event ->
            Stream.of(PreSubmitCallbackStage.values())
                .map(stage -> Arguments.of(event, stage))
        );
    }
}
