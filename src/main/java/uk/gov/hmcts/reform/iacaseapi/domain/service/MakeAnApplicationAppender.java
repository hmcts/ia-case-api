package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class MakeAnApplicationAppender {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;
    private final DateProvider dateProvider;

    public MakeAnApplicationAppender(UserDetails userDetails, UserDetailsHelper userDetailsHelper, DateProvider dateProvider) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.dateProvider = dateProvider;
    }

    public List<IdValue<MakeAnApplication>> append(
        List<IdValue<MakeAnApplication>> existingMakeAnApplications,
        String makeAnApplicationType,
        String makeAnApplicationDesc,
        List<IdValue<Document>> makeAnApplicationEvidence,
        String makeAnApplicationDecision,
        String makeAnApplicationState
    ) {

        requireNonNull(existingMakeAnApplications);
        requireNonNull(makeAnApplicationType);
        requireNonNull(makeAnApplicationDesc);
        requireNonNull(makeAnApplicationEvidence);
        requireNonNull(makeAnApplicationDecision);
        requireNonNull(makeAnApplicationState);

        String applicant = userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString();
        UserRole applicantRole = userDetailsHelper.getLoggedInUserRole(userDetails);

        final MakeAnApplication newMakeAnApplication =
            new MakeAnApplication(applicant, makeAnApplicationType, makeAnApplicationDesc,
                makeAnApplicationEvidence, dateProvider.now().toString(), makeAnApplicationDecision,
                makeAnApplicationState);
        newMakeAnApplication.setApplicantRole(applicantRole.toString());

        final List<IdValue<MakeAnApplication>> allMakeAnApplications =
            new ArrayList<>();

        int index = existingMakeAnApplications.size() + 1;

        allMakeAnApplications.add(new IdValue<>(String.valueOf(index--), newMakeAnApplication));

        for (IdValue<MakeAnApplication> existingMakeAnApplication : existingMakeAnApplications) {
            allMakeAnApplications.add(new IdValue<>(String.valueOf(index--), existingMakeAnApplication.getValue()));
        }

        return allMakeAnApplications;
    }
}
