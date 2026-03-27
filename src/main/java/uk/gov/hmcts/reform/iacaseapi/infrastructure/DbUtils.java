package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DbUtils {

    private static final String EXCEPTION_MESSAGE = "Case ID could not be found from the appeal reference number";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DbUtils(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Retryable(include = TransientDataAccessException.class)
    public String getCaseId(
            String appealReferenceNumber
    ) {

        String type;
        String sequence;
        String year;

        String[] parts = appealReferenceNumber.split("/");
        if (parts.length == 3) {
            type = parts[0];
            sequence = parts[1];
            year = parts[2];
        } else {
            throw new IllegalArgumentException("Invalid appeal reference number format: " + appealReferenceNumber);
        }
        
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("type", type);
        parameters.addValue("year", Integer.parseInt(year));
        parameters.addValue("sequence", Integer.parseInt(sequence));

        try {
            String caseId = selectCaseIdForAppealReferenceNumber(parameters);
            log.info("Retrieved case ID {} for appeal reference number {}", caseId, appealReferenceNumber);
            return caseId;

        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }
    }

    private String selectCaseIdForAppealReferenceNumber(
            MapSqlParameterSource parameters
    ) {
        return jdbcTemplate.queryForObject(
                " SELECT case_id "
                        + " FROM ia_case_api.appeal_reference_numbers "
                        + "   WHERE type = :type "
                        + "     AND year = :year"
                        + "     AND sequence = :sequence;",
                parameters,
                String.class
        );
    }
}
