package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ApplyForCostsProviderTest {
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    private ApplyForCostsProvider applyForCostsProvider;
    private static List<IdValue<Document>> evidence =
            List.of(new IdValue<>("1",
                    new Document("http://localhost/documents/123456",
                            "http://localhost/documents/123456",
                            "DocumentName.pdf")));
    private YesOrNo isApplyForCostsOot = YesOrNo.YES;
    private ApplyForCosts applyForCosts;
    @Mock private AsylumCase asylumCase;

    @BeforeEach
    public void setUp() {
        applyForCostsProvider =
                new ApplyForCostsProvider(userDetails, userDetailsHelper);
        applyForCosts = new ApplyForCosts(
                "Wasted Costs",
                "Evidence Details",
                evidence,
                evidence,
                YesOrNo.YES,
                "Hearing explanation",
                "Pending",
                "Home Office",
                "2023-11-10",
                "Legal Rep Name",
                "OOT explanation",
                evidence,
                YesOrNo.NO,
                "Legal representative");
        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        when(asylumCase.read(AsylumCaseFieldDefinition.APPLIES_FOR_COSTS)).thenReturn(Optional.of(existingAppliesForCosts));
    }

    @Test
    void should_return_apply_costs_in_list_for_LegalRep_loggedIn() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);

        List<Value> applyForCostsForRespondent = applyForCostsProvider.getApplyForCostsForRespondent(asylumCase);
        assertNotNull(applyForCostsForRespondent);
        assertThat(applyForCostsForRespondent.size()).isEqualTo(1);
        assertThat(applyForCostsForRespondent.get(0).getLabel().equals("Costs 1, Wasted Costs, 10 Nov 2023"));
    }

    @ParameterizedTest
    @MethodSource("generateApplyForCostsTestScenarios")
    void should_return_apply_costs_list_suitable_for_additional_evidence(ApplyForCosts applyForCosts, UserRoleLabel role) {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(role);
        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        when(asylumCase.read(AsylumCaseFieldDefinition.APPLIES_FOR_COSTS)).thenReturn(Optional.of(existingAppliesForCosts));

        List<Value> applyForCostsList = applyForCostsProvider.getApplyForCostsForAdditionalEvidence(asylumCase);
        assertNotNull(applyForCostsList);
        assertThat(applyForCostsList.size()).isEqualTo(1);
        assertThat(applyForCostsList.get(0).getLabel().equals("Costs 1, Wasted Costs, 10 Nov 2023"));
    }

    @Test
    void should_return_apply_costs_in_list_for_judge_to_decide() {
        List<Value> applyForCostsForJudgeDecision = applyForCostsProvider.getApplyForCostsForJudgeDecision(asylumCase);
        assertNotNull(applyForCostsForJudgeDecision);
        assertThat(applyForCostsForJudgeDecision.size()).isEqualTo(1);
        assertThat(applyForCostsForJudgeDecision.get(0).getLabel().equals("Costs 1, Wasted Costs, 10 Nov 2023"));
    }

    @Test
    void should_return_empty_list_for_HO_loggedIn() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.HOME_OFFICE_GENERIC);

        List<Value> applyForCostsForRespondent = applyForCostsProvider.getApplyForCostsForRespondent(asylumCase);
        assertNotNull(applyForCostsForRespondent);
        assertThat(applyForCostsForRespondent.size()).isEqualTo(0);
    }

    static Stream<Arguments> generateApplyForCostsTestScenarios() {
        ApplyForCosts applyForCosts1 = new ApplyForCosts(
            "Wasted Costs",
            "Evidence Details",
            evidence,
            evidence,
            YesOrNo.YES,
            "Hearing explanation",
            "Pending",
            "Home office",
            "2023-11-10",
            "Legal Rep Name",
            "OOT explanation",
            evidence,
            YesOrNo.NO,
            "Legal representative");

        ApplyForCosts applyForCosts2 = new ApplyForCosts(
            "Wasted Costs",
            "Evidence Details",
            evidence,
            evidence,
            YesOrNo.YES,
            "Hearing explanation",
            "Pending",
            "Legal representative",
            "2023-11-10",
            "Legal Rep Name",
            "OOT explanation",
            evidence,
            YesOrNo.YES,
            "Home office");
        applyForCosts2.setResponseToApplication("Response to application");

        return Stream.of(
            Arguments.of(applyForCosts1, UserRoleLabel.HOME_OFFICE_GENERIC),
            Arguments.of(applyForCosts2, UserRoleLabel.LEGAL_REPRESENTATIVE)
        );
    }
}