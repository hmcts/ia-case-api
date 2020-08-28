package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DbAppealReferenceNumberGeneratorTest {

    static final int SEQUENCE_SEED = 50000;

    @Mock private DateProvider dateProvider;
    @Mock private NamedParameterJdbcTemplate jdbcTemplate;

    @Captor ArgumentCaptor<MapSqlParameterSource> insertParametersCaptor;
    @Captor ArgumentCaptor<MapSqlParameterSource> selectParametersCaptor;

    final long caseId = 123;
    final AppealType appealType = AppealType.PA;
    final int currentYear = 2017;

    MapSqlParameterSource expectedParameters;
    String expectedAppealReferenceNumber = "PA/12345/2017";

    DbAppealReferenceNumberGenerator dbAppealReferenceNumberGenerator;

    @BeforeEach
    void setUp() {

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
        )).thenThrow(EmptyResultDataAccessException.class);

        assertThatThrownBy(() -> dbAppealReferenceNumberGenerator.generate(caseId, appealType))
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
