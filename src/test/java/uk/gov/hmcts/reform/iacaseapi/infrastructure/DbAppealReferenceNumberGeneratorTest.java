package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
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
            selectParametersCaptor
                .getAllValues()
                .get(0);

        assertEquals(caseId, actualSelectParameters.getValue("caseId"));
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
        )).thenThrow(new DataIntegrityViolationException("Duplicate key violation"));

        String appealReferenceNumber =
            dbAppealReferenceNumberGenerator.generate(caseId, appealType);

        assertEquals(expectedAppealReferenceNumber, appealReferenceNumber);
    }

    @Test
    void should_throw_when_appeal_reference_number_for_case_not_found() {

        when(jdbcTemplate.update(
            and(
                contains("INSERT"),
                contains("INTO ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class)
        )).thenReturn(0);

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
