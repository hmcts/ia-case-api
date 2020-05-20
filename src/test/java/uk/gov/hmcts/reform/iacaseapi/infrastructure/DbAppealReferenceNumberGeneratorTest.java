package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DbAppealReferenceNumberGeneratorTest {

    private static final int SEQUENCE_SEED = 50000;

    @Mock private DateProvider dateProvider;
    @Mock private NamedParameterJdbcTemplate jdbcTemplate;

    @Captor private ArgumentCaptor<MapSqlParameterSource> insertParametersCaptor;
    @Captor private ArgumentCaptor<MapSqlParameterSource> updateParametersCaptor;
    @Captor private ArgumentCaptor<MapSqlParameterSource> selectParametersCaptor;

    private final long caseId = 123;
    private final AppealType appealType = AppealType.PA;
    private final int currentYear = 2017;

    private final String expectedAppealReferenceNumber = "PA/12345/2017";

    private DbAppealReferenceNumberGenerator dbAppealReferenceNumberGenerator;

    @Before
    public void setUp() {

        dbAppealReferenceNumberGenerator =
            new DbAppealReferenceNumberGenerator(
                SEQUENCE_SEED,
                dateProvider,
                jdbcTemplate
            );

        when(dateProvider.now()).thenReturn(LocalDate.of(currentYear, 1, 1));

        MapSqlParameterSource expectedParameters = new MapSqlParameterSource();
        expectedParameters.addValue("caseId", caseId);
        expectedParameters.addValue("appealType", appealType.name());
        expectedParameters.addValue("year", currentYear);
        expectedParameters.addValue("seed", SEQUENCE_SEED);

        when(jdbcTemplate.queryForObject(
            and(
                contains("SELECT"),
                contains("FROM ia_case_api.appeal_reference_numbers")
            ),
            any(MapSqlParameterSource.class),
            eq(String.class)
        )).thenReturn(expectedAppealReferenceNumber);
    }

    @Test
    public void should_call_db_to_update_existing_appeal_reference_number() {

        String appealReferenceNumber = dbAppealReferenceNumberGenerator.update(caseId, appealType);

        assertEquals(expectedAppealReferenceNumber, appealReferenceNumber);

        verify(jdbcTemplate, times(1))
            .update(
                and(
                    contains("UPDATE ia_case_api.appeal_reference_numbers"),
                    contains("SET")
                ),
                updateParametersCaptor.capture()
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

        MapSqlParameterSource actualUpdateParameters = updateParametersCaptor.getAllValues().get(0);
        assertEquals(caseId, actualUpdateParameters.getValue("caseId"));
        assertEquals(appealType.name(), actualUpdateParameters.getValue("appealType"));
        assertEquals(currentYear, actualUpdateParameters.getValue("year"));
        assertEquals(SEQUENCE_SEED, actualUpdateParameters.getValue("seed"));

        MapSqlParameterSource actualSelectParameters = selectParametersCaptor.getAllValues().get(0);
        assertEquals(caseId, actualSelectParameters.getValue("caseId"));
        assertEquals(appealType.name(), actualSelectParameters.getValue("appealType"));
        assertEquals(currentYear, actualSelectParameters.getValue("year"));
    }

    @Test
    public void should_call_db_to_generate_new_appeal_reference_number() {

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
        assertEquals(appealType.name(), actualSelectParameters.getValue("appealType"));
        assertEquals(currentYear, actualSelectParameters.getValue("year"));
    }

    @Test
    public void should_return_existing_appeal_reference_number_already_when_exists() {

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
    public void should_throw_when_appeal_reference_number_for_case_not_found() {

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
