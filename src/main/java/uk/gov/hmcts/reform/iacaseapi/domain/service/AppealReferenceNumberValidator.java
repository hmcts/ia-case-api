package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class AppealReferenceNumberValidator {

    private static final Pattern APPEAL_REF_PATTERN = Pattern.compile("^(HU|DA|DC|EA|PA|RP|LE|LD|LP|LH|LR|IA)/\\d{5}/20\\d{2}$");
    private static final String INVALID_FORMAT_ERROR = "The reference number is in an incorrect format. Please enter valid format of XX/00000/0000";
    private static final String ALREADY_EXISTS_ERROR = "The reference number already exists. Please enter a different reference number.";

    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    public AppealReferenceNumberValidator(AppealReferenceNumberGenerator appealReferenceNumberGenerator) {
        this.appealReferenceNumberGenerator = appealReferenceNumberGenerator;
    }

    /**
     * Validates an appeal reference number for format and existence.
     *
     * @param appealReferenceNumber The reference number to validate in format XX/00000/0000
     * @return List of validation error messages. Empty list if validation passes.
     */
    public List<String> validate(String appealReferenceNumber) {
        List<String> errors = new ArrayList<>();

        if (appealReferenceNumber == null || appealReferenceNumber.isEmpty()) {
            errors.add("Appeal reference number cannot be null or empty");
            return errors;
        }

        // Validate format
        if (!APPEAL_REF_PATTERN.matcher(appealReferenceNumber).matches()) {
            errors.add(INVALID_FORMAT_ERROR);
            return errors; // Don't check existence if format is invalid
        }

        // Check if reference number already exists
        if (appealReferenceNumberGenerator.referenceNumberExists(appealReferenceNumber)) {
            errors.add(ALREADY_EXISTS_ERROR);
        }

        return errors;
    }
}

