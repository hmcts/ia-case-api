package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@Slf4j
@Service
public class DbAppealReferenceNumberGenerator implements AppealReferenceNumberGenerator {

    private static final String EXCEPTION_MESSAGE = "Appeal reference number could not be generated";

    private final int appealReferenceSequenceSeed;
    private final DateProvider dateProvider;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DbAppealReferenceNumberGenerator(
        @Value("${appealReferenceSequenceSeed}") int appealReferenceSequenceSeed,
        DateProvider dateProvider,
        NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.appealReferenceSequenceSeed = appealReferenceSequenceSeed;
        this.dateProvider = dateProvider;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String generate(
        long caseId,
        AppealType appealType) {
        final int currentYear = dateProvider.now().getYear();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("caseId", caseId);
        parameters.addValue("appealType", appealType.name());
        parameters.addValue("year", currentYear);
        parameters.addValue("seed", appealReferenceSequenceSeed);

        try {
            tryInsertNewReferenceNumber(parameters);
        } catch (Exception e) {
            // appeal reference number already exists for this case
            log.warn("There was an issue when the system was generating appeal reference number: {} for case {}", selectAppealReferenceNumberForCase(parameters));
        }

        try {

            String appealReferenceNumber = selectAppealReferenceNumberForCase(parameters);

            log.info("Generated appeal reference number: {} for case {}", appealReferenceNumber, caseId);

            return appealReferenceNumber;

        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }
    }

    @Retryable(include = TransientDataAccessException.class)
    private void tryInsertNewReferenceNumber(
        MapSqlParameterSource parameters
    ) {
        jdbcTemplate.update(
            "INSERT INTO ia_case_api.appeal_reference_numbers "
            + "          (case_id, "
            + "           type, "
            + "           year, "
            + "           sequence) "
            + "   SELECT :caseId, "
            + "          :appealType, "
            + "          :year, "
            + "          COALESCE(MAX(sequence), :seed) + 1 "
            + "    FROM ia_case_api.appeal_reference_numbers "
            + "   WHERE type = :appealType "
            + "     AND year = :year;",
            parameters
        );
    }

    @Retryable(include = TransientDataAccessException.class)
    private String selectAppealReferenceNumberForCase(
        MapSqlParameterSource parameters
    ) {
        return jdbcTemplate.queryForObject(
            " SELECT CONCAT(type, '/', sequence, '/', year) "
            + " FROM ia_case_api.appeal_reference_numbers "
            + "WHERE case_id = :caseId;",
            parameters,
            String.class
        );
    }
}
