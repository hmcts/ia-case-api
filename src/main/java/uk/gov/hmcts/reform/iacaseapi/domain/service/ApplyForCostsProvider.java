package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLIES_FOR_COSTS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

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

    public List<Value> getApplyForCostsForRespondent(AsylumCase asylumCase) {
        Predicate<ApplyForCosts> respondentCondition = applyForCosts ->
            applyForCosts.getApplyForCostsRespondentRole().equals(getLoggedUserRole()) && applyForCosts.getResponseToApplication() == null;

        return getApplyForCosts(asylumCase, respondentCondition);
    }

    public List<Value> getApplyForCostsForAdditionalEvidence(AsylumCase asylumCase) {
        Predicate<ApplyForCosts> applicantOrRespondentCondition = applyForCosts ->
            applyForCosts.getApplyForCostsDecision().equals(CostsDecision.PENDING.toString()) &&
                (applyForCosts.getApplyForCostsApplicantType().equals(getLoggedUserRole()) ||
                    (applyForCosts.getApplyForCostsRespondentRole().equals(getLoggedUserRole()) && StringUtils.isNotBlank(applyForCosts.getResponseToApplication())));

        return getApplyForCosts(asylumCase, applicantOrRespondentCondition);
    }

    public List<Value> getApplyForCostsForJudgeDecision(AsylumCase asylumCase) {
        Predicate<ApplyForCosts> respondentCondition = applyForCosts -> applyForCosts.getApplyForCostsDecision().equals(CostsDecision.PENDING.toString());
        return getApplyForCosts(asylumCase, respondentCondition);
    }

    public List<Value> getApplyForCosts(AsylumCase asylumCase, Predicate<ApplyForCosts> condition) {
        Optional<List<IdValue<ApplyForCosts>>> existingApplyForCosts =
            asylumCase.read(APPLIES_FOR_COSTS);

        List<Value> applyForCostsForRespondent = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        existingApplyForCosts
            .orElse(Collections.emptyList())
            .forEach(idValue -> {
                ApplyForCosts applyForCosts = idValue.getValue();
                if (condition.test(applyForCosts)) {
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

    public String formatDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    public String getLoggedUserRole() {
        String loggedInUser = userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString();
        return loggedInUser.equals("Respondent") ? "Home office" : loggedInUser;
    }
}
