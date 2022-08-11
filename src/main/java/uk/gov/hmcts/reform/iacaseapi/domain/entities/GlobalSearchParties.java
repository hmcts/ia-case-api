package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class GlobalSearchParties {

    private String appellantTitle;
    private String appellantGivenNames;
    private String appellantFamilyName;
    private String appellantAddress;
    private String appellantEmailAddress;
    private String appellantDateOfBirth;

    private GlobalSearchParties() {
        // noop -- for deserializer
    }

    public GlobalSearchParties(
        String appellantTitle,
        String appellantGivenNames,
        String appellantFamilyName,
        String appellantAddress,
        String appellantEmailAddress,
        String appellantDateOfBirth

    ) {
        requireNonNull(appellantTitle);
        requireNonNull(appellantGivenNames);
        requireNonNull(appellantFamilyName);
        requireNonNull(appellantAddress);
        requireNonNull(appellantEmailAddress);
        requireNonNull(appellantDateOfBirth);

        this.appellantTitle = appellantTitle;
        this.appellantGivenNames = appellantGivenNames;
        this.appellantFamilyName = appellantFamilyName;
        this.appellantAddress = appellantAddress;
        this.appellantEmailAddress = appellantEmailAddress;
        this.appellantDateOfBirth = appellantDateOfBirth;
    }

}
