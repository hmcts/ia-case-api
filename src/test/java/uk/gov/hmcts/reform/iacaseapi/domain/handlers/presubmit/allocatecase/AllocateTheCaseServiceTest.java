package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Value;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class AllocateTheCaseServiceTest {

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void isAllocateToCaseWorkerOption(Scenario scenario) {
        AsylumCase asylumCase = mock(AsylumCase.class);
        AllocateTheCaseService allocateTheCaseService = new AllocateTheCaseService();

        when(asylumCase.read(eq(ALLOCATE_THE_CASE_TO), eq(String.class)))
            .thenReturn(Optional.ofNullable(scenario.allocateTheCaseTo));

        boolean actual = allocateTheCaseService.isAllocateToCaseWorkerOption(asylumCase);

        assertThat(actual).isEqualTo(scenario.expectedResult);
    }

    private static Stream<Scenario> scenarioProvider() {
        Scenario happyPathScenario = new Scenario("caseworker", true);
        Scenario fieldIsNullScenario = new Scenario(null, false);
        Scenario fieldIsEmptyScenario = new Scenario("", false);

        return Stream.of(happyPathScenario, fieldIsNullScenario, fieldIsEmptyScenario);
    }

    @Value
    private static class Scenario {
        String allocateTheCaseTo;
        boolean expectedResult;
    }
}