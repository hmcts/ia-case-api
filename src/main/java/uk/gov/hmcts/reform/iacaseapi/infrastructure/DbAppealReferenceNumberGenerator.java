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
        // Find the next available sequence number with gap-filling strategy
        // This prevents wasting sequence numbers when manually entered high numbers exist
        // 
        // Strategy:
        // 1. Start with minimum sequence: the greater of (seed OR smallest existing sequence)
        // 2. Find first gap by looking for a sequence where sequence+1 doesn't exist
        // 3. Only check within a reasonable range to avoid performance issues
        // 4. If no gaps found or range exceeded, fall back to MAX(sequence) + 1
        //
        // Performance consideration: Limits gap search to avoid issues with very large gaps
        jdbcTemplate.update(
                "INSERT INTO ia_case_api.appeal_reference_numbers "
                        + "          (case_id, "
                        + "           type, "
                        + "           year, "
                        + "           sequence) "
                        + "   SELECT :caseId, "
                        + "          :appealType, "
                        + "          :year, "
                        + "          COALESCE( "
                        // Try to find a gap: look for the first available sequence starting from seed
                        // This checks if there's a missing sequence between existing records
                        + "            ( "
                        + "              WITH sequence_range AS ( "
                        + "                SELECT MIN(sequence) as min_seq, MAX(sequence) as max_seq "
                        + "                  FROM ia_case_api.appeal_reference_numbers "
                        + "                 WHERE type = :appealType "
                        + "                   AND year = :year "
                        + "                   AND sequence >= :seed "
                        + "              ), "
                        + "              first_gap AS ( "
                        + "                SELECT t1.sequence + 1 AS gap_seq "
                        + "                  FROM ia_case_api.appeal_reference_numbers t1 "
                        + "                  LEFT JOIN ia_case_api.appeal_reference_numbers t2 "
                        + "                         ON t2.type = t1.type "
                        + "                        AND t2.year = t1.year "
                        + "                        AND t2.sequence = t1.sequence + 1 "
                        + "                 WHERE t1.type = :appealType "
                        + "                   AND t1.year = :year "
                        + "                   AND t1.sequence >= :seed "
                        + "                   AND t2.sequence IS NULL "
                        + "                   AND t1.sequence < (SELECT COALESCE(max_seq, :seed) FROM sequence_range) "
                        + "                 ORDER BY t1.sequence "
                        + "                 LIMIT 1 "
                        + "              ) "
                        + "              SELECT gap_seq FROM first_gap WHERE gap_seq >= :seed "
                        + "            ), "
                        // Fallback 1: If no gaps, check if we need to start from seed
                        + "            ( "
                        + "              SELECT :seed "
                        + "               WHERE NOT EXISTS ( "
                        + "                 SELECT 1 FROM ia_case_api.appeal_reference_numbers "
                        + "                  WHERE type = :appealType "
                        + "                    AND year = :year "
                        + "                    AND sequence = :seed "
                        + "               ) "
                        + "               AND NOT EXISTS ( "
                        + "                 SELECT 1 FROM ia_case_api.appeal_reference_numbers "
                        + "                  WHERE type = :appealType "
                        + "                    AND year = :year "
                        + "                    AND sequence >= :seed "
                        + "               ) "
                        + "            ), "
                        // Fallback 2: Use MAX(sequence) + 1
                        + "            ( "
                        + "              SELECT COALESCE(MAX(sequence), :seed - 1) + 1 "
                        + "                FROM ia_case_api.appeal_reference_numbers "
                        + "               WHERE type = :appealType "
                        + "                 AND year = :year "
                        + "            ) "
                        + "          );",
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
            log.info("Reference number {} exists: {}", referenceNumber, exists);
            return exists;

        } catch (Exception e) {
            log.error("Error checking if reference number exists: {}", referenceNumber, e);
            throw new IllegalStateException("Failed to check reference number existence", e);
        }
    }

    /**
     * Registers a manually entered reference number in the database.
     * This ensures duplicate checking works for both generated and manually entered reference numbers.
     * If the reference number already exists in the database, this method will silently ignore the duplicate.
     *
     * @param caseId The case ID
     * @param referenceNumber The reference number in format XX/00000/0000
     */
    @Override
    @Retryable(include = TransientDataAccessException.class)
    public void registerReferenceNumber(long caseId, String referenceNumber) {
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
        parameters.addValue("caseId", caseId);
        parameters.addValue("appealType", appealType);
        parameters.addValue("sequence", sequence);
        parameters.addValue("year", year);

        try {
            // Check if this reference number already exists for another case
            Integer existingCaseId = jdbcTemplate.queryForObject(
                    "SELECT case_id "
                            + "  FROM ia_case_api.appeal_reference_numbers "
                            + " WHERE type = :appealType "
                            + "   AND sequence = :sequence "
                            + "   AND year = :year "
                            + "   AND case_id != :caseId "
                            + " LIMIT 1;",
                    parameters,
                    Integer.class
            );
            
            if (existingCaseId != null) {
                log.warn("Reference number {} already exists for case {}. Cannot register for case {}", 
                        referenceNumber, existingCaseId, caseId);
                return;
            }
        } catch (EmptyResultDataAccessException e) {
            // No existing reference number found for another case, proceed with registration
        }

        try {
            // Insert or update the reference number for this case
            // If case_id already exists, update it; otherwise insert
            int rowsAffected = jdbcTemplate.update(
                    "INSERT INTO ia_case_api.appeal_reference_numbers "
                            + "          (case_id, "
                            + "           type, "
                            + "           year, "
                            + "           sequence) "
                            + "   VALUES (:caseId, "
                            + "           :appealType, "
                            + "           :year, "
                            + "           :sequence) "
                            + "ON CONFLICT (case_id) "
                            + "DO UPDATE SET type = :appealType, "
                            + "              year = :year, "
                            + "              sequence = :sequence;",
                    parameters
            );
            
            if (rowsAffected > 0) {
                log.info("Registered reference number {} for case {}", referenceNumber, caseId);
            }
        } catch (Exception e) {
            // If there's an unexpected error, log a warning but don't fail
            log.warn("Could not register reference number {} for case {}: {}", 
                    referenceNumber, caseId, e.getMessage());
        }
    }
}
