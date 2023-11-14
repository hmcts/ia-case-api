package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLIES_FOR_COSTS;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ApplyForCostsProvider {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public ApplyForCostsProvider(
        UserDetails userDetails,
        UserDetailsHelper userDetailsHelper
    ) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    public List<Value> getApplyForCosts(AsylumCase asylumCase) {
        String loggedInUser = userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString();

        Optional<List<IdValue<ApplyForCosts>>> existingApplyForCosts =
                asylumCase.read(APPLIES_FOR_COSTS);
        final String user = loggedInUser.equals("Respondent") ? "Home office" : loggedInUser;

        List<Value> applyForCostsForRespondent = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        existingApplyForCosts
                .orElse(Collections.emptyList())
                .stream()
                .forEach(idValue -> {
                    ApplyForCosts applyForCosts = idValue.getValue();
                    if (applyForCosts.getApplyForCostsRespondentRole().equals(user) && applyForCosts.getResponseToApplication() == null) {
                        applyForCostsForRespondent.add(
                                new Value(idValue.getId(), "Costs " + counter + ", " + applyForCosts.getAppliedCostsType() + ", " + formatDate(applyForCosts.getApplyForCostsCreationDate())));
                    }
                    counter.getAndIncrement();
                });

        return applyForCostsForRespondent;
    }

    // format date string to pattern dd MMM YYYY
    public String formatDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return localDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

}
