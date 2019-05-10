package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
public class DbAppealReferenceNumberGeneratorIntegrationTest {

    @MockBean private DateProvider dateProvider;

    @Autowired private DbAppealReferenceNumberGenerator dbAppealReferenceNumberGenerator;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {

        deleteAnyTestAppealReferenceNumbers();

        when(dateProvider.now()).thenReturn(LocalDate.of(2018, 12, 31));
    }

    @Test
    public void should_start_from_offset_to_skip_any_live_cases_when_2019() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 12, 31));

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.RP);

        assertThat(firstAppealReferenceNumber, is("PA/50020/2019"));
        assertThat(secondAppealReferenceNumber, is("RP/50020/2019"));
    }

    @Test
    public void should_generate_sequential_appeal_reference_number_for_protection_appeal() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.PA);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.PA);

        assertThat(firstAppealReferenceNumber, is("PA/50001/2018"));
        assertThat(secondAppealReferenceNumber, is("PA/50002/2018"));
        assertThat(thirdAppealReferenceNumber, is("PA/50003/2018"));
    }

    @Test
    public void should_generate_sequential_appeal_reference_number_for_revocation_appeal() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.RP);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.RP);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.RP);

        assertThat(firstAppealReferenceNumber, is("RP/50001/2018"));
        assertThat(secondAppealReferenceNumber, is("RP/50002/2018"));
        assertThat(thirdAppealReferenceNumber, is("RP/50003/2018"));
    }

    @Test
    public void should_use_distinct_number_range_for_each_appeal_type() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.RP);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.PA);

        final String fourthAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(4, AppealType.RP);

        assertThat(firstAppealReferenceNumber, is("PA/50001/2018"));
        assertThat(secondAppealReferenceNumber, is("RP/50001/2018"));
        assertThat(thirdAppealReferenceNumber, is("PA/50002/2018"));
        assertThat(fourthAppealReferenceNumber, is("RP/50002/2018"));
    }

    @Test
    public void should_always_return_same_appeal_reference_number_for_same_case() {

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        assertThat(firstAppealReferenceNumber, is("PA/50001/2018"));
        assertThat(secondAppealReferenceNumber, is("PA/50001/2018"));
        assertThat(thirdAppealReferenceNumber, is("PA/50001/2018"));
    }

    @Test
    public void should_reset_number_range_using_seed_for_new_years() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2022, 12, 31));

        final String firstAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        when(dateProvider.now()).thenReturn(LocalDate.of(2023, 01, 01));

        final String secondAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(2, AppealType.PA);

        when(dateProvider.now()).thenReturn(LocalDate.of(2024, 12, 31));

        final String thirdAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(3, AppealType.PA);

        assertThat(firstAppealReferenceNumber, is("PA/50001/2022"));
        assertThat(secondAppealReferenceNumber, is("PA/50001/2023"));
        assertThat(thirdAppealReferenceNumber, is("PA/50001/2024"));
    }

    @Test
    public void should_not_create_duplicate_appeal_reference_numbers_when_used_concurrently() throws InterruptedException, ExecutionException {

        Set<String> appealReferenceNumbers =
            (new ForkJoinPool(32))
                .submit(() ->
                    LongStream.rangeClosed(1000000000000001L, 1000000000000000L + 10000L)
                        .parallel()
                        .mapToObj(caseId -> dbAppealReferenceNumberGenerator.generate(caseId, AppealType.PA))
                        .collect(Collectors.toSet())
                ).get();

        NavigableSet<String> sortedAppealReferenceNumbers = new TreeSet<>(appealReferenceNumbers);

        assertThat(sortedAppealReferenceNumbers.size(), is(10000));
        assertThat(sortedAppealReferenceNumbers.pollFirst(), is("PA/50001/2018"));
        assertThat(sortedAppealReferenceNumbers.pollLast(), is("PA/60000/2018"));
    }

    @Test
    public void should_return_original_appeal_reference_number_when_same_case_is_presented_with_different_appeal_type() {

        final String originalAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        final String subsequentAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.RP);

        assertThat(originalAppealReferenceNumber, is("PA/50001/2018"));
        assertThat(subsequentAppealReferenceNumber, is("PA/50001/2018"));
    }

    @Test
    public void should_return_original_appeal_reference_number_when_same_case_is_presented_with_different_year() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2018, 12, 31));

        final String originalAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 01, 01));

        final String subsequentAppealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(1, AppealType.PA);

        assertThat(originalAppealReferenceNumber, is("PA/50001/2018"));
        assertThat(subsequentAppealReferenceNumber, is("PA/50001/2018"));
    }

    private void deleteAnyTestAppealReferenceNumbers() {
        jdbcTemplate.execute("DELETE FROM ia_case_api.appeal_reference_numbers WHERE case_id NOT IN (-1, -2);");
    }
}
