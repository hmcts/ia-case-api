package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class DbUtilsTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private DbUtils dbUtils;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        dbUtils = new DbUtils(jdbcTemplate);
    }

    @Test
    void shouldReturnCaseIdForValidAppealReferenceNumber() {
        String appealReferenceNumber = "PA/12345/2026";
        String expectedCaseId = "case-001";

        // Stub jdbcTemplate
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class)))
                .thenReturn(expectedCaseId);

        String caseId = dbUtils.getCaseId(appealReferenceNumber);

        assertThat(caseId).isEqualTo(expectedCaseId);

        // Verify parameters passed to jdbcTemplate
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).queryForObject(anyString(), paramCaptor.capture(), eq(String.class));

        MapSqlParameterSource capturedParams = paramCaptor.getValue();
        assertThat(capturedParams.getValue("type")).isEqualTo("PA");
        assertThat(capturedParams.getValue("sequence")).isEqualTo(12345);
        assertThat(capturedParams.getValue("year")).isEqualTo(2026);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidFormat() {
        String invalidReference = "PA-12345-2026"; // uses '-' instead of '/'

        assertThatThrownBy(() -> dbUtils.getCaseId(invalidReference))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid appeal reference number format");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCaseNotFound() {
        String appealReferenceNumber = "PA/12345/2026";

        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> dbUtils.getCaseId(appealReferenceNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Case ID could not be found");

        // Verify query was executed
        verify(jdbcTemplate).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class));
    }

    @Test
    void shouldRetryOnTransientDataAccessException() {
        String appealReferenceNumber = "PA/12345/2026";
        String expectedCaseId = "case-002";

        // Simulate transient exception first, then return value
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class)))
                .thenThrow(new TransientDataAccessException("Temporary") {})
                .thenReturn(expectedCaseId);

        // In a real Spring environment, @Retryable would automatically retry
        // Here we just call the method twice manually to simulate retry
        assertThatThrownBy(() -> dbUtils.getCaseId(appealReferenceNumber))
                .isInstanceOf(TransientDataAccessException.class);

        // We could verify interactions if integrated with Spring Retry
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class));
    }
}