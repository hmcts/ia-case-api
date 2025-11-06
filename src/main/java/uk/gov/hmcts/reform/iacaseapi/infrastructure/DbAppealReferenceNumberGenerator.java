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

    @Override
    @Retryable(include = TransientDataAccessException.class)
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

    /**
     * Checks if a reference number already exists in the database.
     *
     * @param referenceNumber The reference number in format XX/00000/0000
     * @return true if the reference number exists, false otherwise
     */
    @Override
    public boolean referenceNumberExists(String referenceNumber) {
        if (referenceNumber == null || referenceNumber.isEmpty()) {
            throw new IllegalArgumentException("Reference number cannot be null or empty");
        }

        String[] parts = referenceNumber.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid reference number format. Expected format: XX/00000/0000");
        }

        String appealType = parts[0];
        int sequence;
        int year;
        try {
            sequence = Integer.parseInt(parts[1]);
            year = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid reference number format. Expected format: XX/00000/0000", e);
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("appealType", appealType);
        parameters.addValue("sequence", sequence);
        parameters.addValue("year", year);

        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) "
                            + "  FROM ia_case_api.appeal_reference_numbers "
                            + " WHERE type = :appealType "
                            + "   AND sequence = :sequence "
                            + "   AND year = :year;",
                    parameters,
                    Integer.class
            );

            boolean exists = count != null && count > 0;
            log.debug("Reference number {} exists: {}", referenceNumber, exists);
            return exists;

        } catch (Exception e) {
            log.error("Error checking if reference number exists: {}", referenceNumber, e);
            throw new IllegalStateException("Failed to check reference number existence", e);
        }
    }
}
