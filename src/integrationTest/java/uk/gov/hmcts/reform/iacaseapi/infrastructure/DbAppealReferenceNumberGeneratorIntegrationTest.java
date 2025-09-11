package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

class DbAppealReferenceNumberGeneratorIntegrationTest extends SpringBootIntegrationTest {

    @MockBean
    private DateProvider dateProvider;

    @Autowired
    private DbAppealReferenceNumberGenerator dbAppealReferenceNumberGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        deleteAnyTestAppealReferenceNumbers();

        when(dateProvider.now()).thenReturn(LocalDate.of(2018, 12, 31));
    }

    @Test
    void should_start_from_offset_to_skip_any_live_cases_when_2019() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 12, 31));

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.RP);

        assertThat(firstAppealReferenceNumber).contains("PA/50020/2019");
        assertThat(secondAppealReferenceNumber).contains("RP/50020/2019");
    }

    @Test
    void should_generate_sequential_appeal_reference_number_for_protection_appeal() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.PA);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.PA);

        assertThat(firstAppealReferenceNumber).contains("PA/50001/2018");
        assertThat(secondAppealReferenceNumber).contains("PA/50002/2018");
        assertThat(thirdAppealReferenceNumber).contains("PA/50003/2018");
    }

    @Test
    void should_generate_sequential_appeal_reference_number_for_revocation_appeal() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.RP);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.RP);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.RP);

        assertThat(firstAppealReferenceNumber).contains("RP/50001/2018");
        assertThat(secondAppealReferenceNumber).contains("RP/50002/2018");
        assertThat(thirdAppealReferenceNumber).contains("RP/50003/2018");
    }

    @Test
    void should_use_distinct_number_range_for_each_appeal_type() {

        final String appealReferenceNumber1 =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String appealReferenceNumber2 =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.RP);

        final String appealReferenceNumber3 =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.HU);

        final String appealReferenceNumber4 =
                dbAppealReferenceNumberGenerator.generate(4, AppealType.EA);

        final String appealReferenceNumber5 =
                dbAppealReferenceNumberGenerator.generate(5, AppealType.EU);

        final String appealReferenceNumber6 =
            dbAppealReferenceNumberGenerator.generate(6, AppealType.PA);

        final String appealReferenceNumber7 =
            dbAppealReferenceNumberGenerator.generate(7, AppealType.RP);

        final String appealReferenceNumber8 =
            dbAppealReferenceNumberGenerator.generate(8, AppealType.HU);

        final String appealReferenceNumber9 =
                dbAppealReferenceNumberGenerator.generate(9, AppealType.EA);

        final String appealReferenceNumber10 =
                dbAppealReferenceNumberGenerator.generate(10, AppealType.EU);

        assertThat(appealReferenceNumber1).contains("PA/50001/2018");
        assertThat(appealReferenceNumber2).contains("RP/50001/2018");
        assertThat(appealReferenceNumber3).contains("HU/50001/2018");
        assertThat(appealReferenceNumber4).contains("EA/50001/2018");
        assertThat(appealReferenceNumber5).contains("EU/50001/2018");
        assertThat(appealReferenceNumber6).contains("PA/50002/2018");
        assertThat(appealReferenceNumber7).contains("RP/50002/2018");
        assertThat(appealReferenceNumber8).contains("HU/50002/2018");
        assertThat(appealReferenceNumber9).contains("EA/50002/2018");
        assertThat(appealReferenceNumber10).contains("EU/50002/2018");
    }

    @Test
    void should_always_return_same_appeal_reference_number_for_same_case() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        assertThat(firstAppealReferenceNumber).contains("PA/50001/2018");
        assertThat(secondAppealReferenceNumber).contains("PA/50001/2018");
        assertThat(thirdAppealReferenceNumber).contains("PA/50001/2018");
    }

    @Test
    void should_reset_number_range_using_seed_for_new_years() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2022, 12, 31));

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        when(dateProvider.now()).thenReturn(LocalDate.of(2023, 01, 01));

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.PA);

        when(dateProvider.now()).thenReturn(LocalDate.of(2024, 12, 31));

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.PA);

        assertThat(firstAppealReferenceNumber).contains("PA/50001/2022");
        assertThat(secondAppealReferenceNumber).contains("PA/50001/2023");
        assertThat(thirdAppealReferenceNumber).contains("PA/50001/2024");
    }

    @Test
    void should_not_create_duplicate_appeal_reference_numbers_when_used_concurrently()
        throws InterruptedException, ExecutionException {

        Set<String> appealReferenceNumbers =
            (new ForkJoinPool(32))
                .submit(() ->
                    LongStream.rangeClosed(1000000000000001L, 1000000000000000L + 10000L)
                        .parallel()
                        .mapToObj(caseId -> dbAppealReferenceNumberGenerator.generate(caseId, AppealType.PA))
                        .collect(Collectors.toSet())
                ).get();

        NavigableSet<String> sortedAppealReferenceNumbers = new TreeSet<>(appealReferenceNumbers);

        assertThat(sortedAppealReferenceNumbers.size()).isEqualTo(10000);
        assertThat(sortedAppealReferenceNumbers.pollFirst()).contains("PA/50001/2018");
        assertThat(sortedAppealReferenceNumbers.pollLast()).contains("PA/60000/2018");
    }

    @Test
    void should_return_original_appeal_reference_number_when_same_case_is_presented_with_different_appeal_type() {

        final String originalAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String subsequentAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.RP);

        final String subsequentDetainedAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.RP);

        assertThat(originalAppealReferenceNumber).contains("PA/50001/2018");
        assertThat(subsequentAppealReferenceNumber).contains("PA/50001/2018");
        assertThat(subsequentDetainedAppealReferenceNumber).contains("PA/50001/2018");
    }

    @Test
    void should_return_original_appeal_reference_number_when_same_case_is_presented_with_different_year() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2018, 12, 31));

        final String originalAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 01, 01));

        final String subsequentAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        assertThat(originalAppealReferenceNumber).contains("PA/50001/2018");
        assertThat(subsequentAppealReferenceNumber).contains("PA/50001/2018");
    }

    private void deleteAnyTestAppealReferenceNumbers() {
        jdbcTemplate.execute("DELETE FROM ia_case_api.appeal_reference_numbers WHERE case_id NOT IN (-1, -2);");
    }

}
