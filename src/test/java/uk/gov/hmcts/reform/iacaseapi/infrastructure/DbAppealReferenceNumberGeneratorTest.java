package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DbAppealReferenceNumberGeneratorTest {

    private static final int SEQUENCE_SEED = 50000;
    private final long caseId = 123;
    private final AppealType appealType = AppealType.PA;
    private final int currentYear = 2017;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Captor
    private ArgumentCaptor<MapSqlParameterSource> insertParametersCaptor;
    @Captor
    private ArgumentCaptor<MapSqlParameterSource> selectParametersCaptor;
    private MapSqlParameterSource expectedParameters;
    private String expectedAppealReferenceNumber = "PA/12345/2017";
    private String expectedDetainedAppealReferenceNumber = "DE/12345/2017";

    private DbAppealReferenceNumberGenerator dbAppealReferenceNumberGenerator;

    @BeforeEach
    public void setUp() {

        dbAppealReferenceNumberGenerator =
            new DbAppealReferenceNumberGenerator(
                SEQUENCE_SEED,
                dateProvider,
                jdbcTemplate
            );

        when(dateProvider.now()).thenReturn(LocalDate.of(currentYear, 1, 1));

        expectedParameters = new MapSqlParameterSource();
        expectedParameters.addValue("caseId", caseId);
        expectedParameters.addValue("appealType", appealType.name());
        expectedParameters.addValue("year", currentYear);
        expectedParameters.addValue("seed", SEQUENCE_SEED);
    }

    @Test
    void should_call_db_to_generate_new_appeal_reference_number() {

        when(jdbcTemplate.queryForObject(
            and(
                contains("SELECT"),
                contains("FROM ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class),
            eq(String.class)
        )).thenReturn(expectedAppealReferenceNumber);

        String appealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(caseId, appealType);

        assertEquals(expectedAppealReferenceNumber, appealReferenceNumber);

        verify(jdbcTemplate, times(1))
            .update(
                and(
                    contains("INSERT"),
                    contains("INTO ia_case_api.appeal_reference_numbers")
                ),
                insertParametersCaptor.capture()
            );

        verify(jdbcTemplate, times(1))
            .queryForObject(
                and(
                    contains("SELECT"),
                    contains("FROM ia_case_api.appeal_reference_numbers")
                ),
                selectParametersCaptor.capture(),
                eq(String.class)
            );

        MapSqlParameterSource actualInsertParameters =
            insertParametersCaptor
                .getAllValues()
                .get(0);

        assertEquals(caseId, actualInsertParameters.getValue("caseId"));
        assertEquals(appealType.name(), actualInsertParameters.getValue("appealType"));
        assertEquals(currentYear, actualInsertParameters.getValue("year"));
        assertEquals(SEQUENCE_SEED, actualInsertParameters.getValue("seed"));

        MapSqlParameterSource actualSelectParameters =
            insertParametersCaptor
                .getAllValues()
                .get(0);

        assertEquals(caseId, actualSelectParameters.getValue("caseId"));
        assertEquals(appealType.name(), actualSelectParameters.getValue("appealType"));
        assertEquals(currentYear, actualSelectParameters.getValue("year"));
    }

    @Test
    void should_return_existing_appeal_reference_number_already_when_exists() {

        when(jdbcTemplate.queryForObject(
            and(
                contains("SELECT"),
                contains("FROM ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class),
            eq(String.class)
        )).thenReturn(expectedAppealReferenceNumber);

        when(jdbcTemplate.update(
            and(
                contains("INSERT"),
                contains("INTO ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class)
        )).thenThrow(DuplicateKeyException.class);

        String appealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(caseId, appealType);

        assertEquals(expectedAppealReferenceNumber, appealReferenceNumber);
    }

    @Test
    void should_throw_when_appeal_reference_number_for_case_not_found() {

        when(jdbcTemplate.queryForObject(
            and(
                contains("SELECT"),
                contains("FROM ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class),
            eq(String.class)
        )).thenReturn(expectedAppealReferenceNumber);

        when(jdbcTemplate.queryForObject(
            and(
                contains("SELECT"),
                contains("FROM ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class),
            eq(String.class)
        )).thenThrow(EmptyResultDataAccessException.class);

        assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.generate(caseId, appealType))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Nested
    class ReferenceNumberExists {

        @Test
        void should_return_true_when_reference_number_exists() {
            String referenceNumber = "PA/12345/2017";

            when(jdbcTemplate.queryForObject(
                and(
                    contains("SELECT COUNT(*)"),
                    contains("FROM ia_case_api.appeal_reference_numbers")
                ),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
            )).thenReturn(1);

            boolean exists = dbAppealReferenceNumberGenerator.referenceNumberExists(referenceNumber);

            assertTrue(exists);
            verify(jdbcTemplate, times(1))
                .queryForObject(
                    contains("SELECT COUNT(*)"),
                    any(MapSqlParameterSource.class),
                    eq(Integer.class)
                );
        }

        @Test
        void should_return_false_when_reference_number_does_not_exist() {
            String referenceNumber = "HU/99999/2025";

            when(jdbcTemplate.queryForObject(
                and(
                    contains("SELECT COUNT(*)"),
                    contains("FROM ia_case_api.appeal_reference_numbers")
                ),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
            )).thenReturn(0);

            boolean exists = dbAppealReferenceNumberGenerator.referenceNumberExists(referenceNumber);

            assertFalse(exists);
        }

        @Test
        void should_return_false_when_count_is_null() {
            String referenceNumber = "EA/54321/2020";

            when(jdbcTemplate.queryForObject(
                any(String.class),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
            )).thenReturn(null);

            boolean exists = dbAppealReferenceNumberGenerator.referenceNumberExists(referenceNumber);

            assertFalse(exists);
        }

        @Test
        void should_throw_exception_when_reference_number_is_null() {
            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reference number cannot be null or empty");
        }

        @Test
        void should_throw_exception_when_reference_number_is_empty() {
            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(""))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reference number cannot be null or empty");
        }

        @Test
        void should_throw_exception_when_reference_number_has_invalid_format_too_few_parts() {
            String invalidReferenceNumber = "PA/12345";

            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(invalidReferenceNumber))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid reference number format. Expected format: XX/00000/0000");
        }

        @Test
        void should_throw_exception_when_reference_number_has_invalid_format_too_many_parts() {
            String invalidReferenceNumber = "PA/12345/2017/extra";

            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(invalidReferenceNumber))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid reference number format. Expected format: XX/00000/0000");
        }

        @Test
        void should_throw_exception_when_reference_number_has_no_slashes() {
            String invalidReferenceNumber = "PA123452017";

            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(invalidReferenceNumber))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid reference number format. Expected format: XX/00000/0000");
        }

        @Test
        void should_throw_exception_when_database_error_occurs() {
            String referenceNumber = "DE/11111/2018";

            when(jdbcTemplate.queryForObject(
                any(String.class),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
            )).thenThrow(new DataAccessException("Database connection error") {});

            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(referenceNumber))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to check reference number existence")
                .hasCauseInstanceOf(DataAccessException.class);
        }

        @Test
        void should_verify_correct_parameters_passed_to_query() {
            String referenceNumber = "RP/67890/2019";
            ArgumentCaptor<MapSqlParameterSource> parametersCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

            when(jdbcTemplate.queryForObject(
                any(String.class),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
            )).thenReturn(1);

            dbAppealReferenceNumberGenerator.referenceNumberExists(referenceNumber);

            verify(jdbcTemplate).queryForObject(
                any(String.class),
                parametersCaptor.capture(),
                eq(Integer.class)
            );

            MapSqlParameterSource capturedParameters = parametersCaptor.getValue();
            assertEquals("RP", capturedParameters.getValue("appealType"));
            assertEquals(67890, capturedParameters.getValue("sequence"));
            assertEquals(2019, capturedParameters.getValue("year"));
        }

        @Test
        void should_handle_detained_appeal_type() {
            String referenceNumber = "DE/12345/2017";

            when(jdbcTemplate.queryForObject(
                any(String.class),
                any(MapSqlParameterSource.class),
                eq(Integer.class)
            )).thenReturn(1);

            boolean exists = dbAppealReferenceNumberGenerator.referenceNumberExists(referenceNumber);

            assertTrue(exists);
        }

        @Test
        void should_throw_exception_when_sequence_is_not_numeric() {
            String invalidReferenceNumber = "PA/abc123/2017";

            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(invalidReferenceNumber))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid reference number format");
        }

        @Test
        void should_throw_exception_when_year_is_not_numeric() {
            String invalidReferenceNumber = "PA/12345/abcd";

            assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.referenceNumberExists(invalidReferenceNumber))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid reference number format");
        }
    }
}
