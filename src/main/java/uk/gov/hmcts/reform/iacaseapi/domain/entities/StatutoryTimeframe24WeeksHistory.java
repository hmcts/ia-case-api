package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class StatutoryTimeframe24WeeksHistory {

    private YesOrNo status;
    private String reason;
    private String homeOfficeCohort;
    private String user;
    private String dateTimeAdded;

    private StatutoryTimeframe24WeeksHistory() {
    }

    public StatutoryTimeframe24WeeksHistory(
        YesOrNo status,
        String reason,
        String homeOfficeCohort,
        String user,
        String dateTimeAdded
    ) {
        this.status = requireNonNull(status);
        this.reason = requireNonNull(reason);
        this.homeOfficeCohort = requireNonNull(homeOfficeCohort);
        this.user = requireNonNull(user);
        this.dateTimeAdded = requireNonNull(dateTimeAdded);
    }

    public YesOrNo getStatus() {
        return requireNonNull(status);
    }

    public String getReason() {
        return requireNonNull(reason);
    }

    public String getHomeOfficeCohort() {
        return requireNonNull(homeOfficeCohort);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateTimeAdded() {
        return requireNonNull(dateTimeAdded);
    }
}
