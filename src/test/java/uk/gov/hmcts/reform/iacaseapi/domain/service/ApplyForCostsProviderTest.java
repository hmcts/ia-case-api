package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
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
    @Mock
    private AsylumCase asylumCase;

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
        assertEquals(1, applyForCostsForRespondent.size());
        assertEquals("Costs 1, Wasted Costs, 10 Nov 2023", applyForCostsForRespondent.getFirst().getLabel());
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
        assertEquals(1, applyForCostsList.size());
        assertEquals("Costs 1, Wasted Costs, 10 Nov 2023", applyForCostsList.getFirst().getLabel());
    }

    @Test
    void should_return_apply_costs_in_list_for_judge_to_decide() {
        List<Value> applyForCostsForJudgeDecision = applyForCostsProvider.getApplyForCostsForJudgeDecision(asylumCase);
        assertNotNull(applyForCostsForJudgeDecision);
        assertEquals(1, applyForCostsForJudgeDecision.size());
        assertEquals("Costs 1, Wasted Costs, 10 Nov 2023", applyForCostsForJudgeDecision.getFirst().getLabel());
    }

    @Test
    void should_return_empty_list_for_HO_loggedIn() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.HOME_OFFICE_GENERIC);

        List<Value> applyForCostsForRespondent = applyForCostsProvider.getApplyForCostsForRespondent(asylumCase);
        assertNotNull(applyForCostsForRespondent);
        assertTrue(applyForCostsForRespondent.isEmpty());
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
            "Legal representative"
        );

        ApplyForCosts applyForCosts2 = new ApplyForCosts(
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
            "Legal representative"
        );
        applyForCosts2.setResponseToApplication("Response to application");

        return Stream.of(
            Arguments.of(applyForCosts1, UserRoleLabel.HOME_OFFICE_GENERIC),
            Arguments.of(applyForCosts2, UserRoleLabel.LEGAL_REPRESENTATIVE)
        );
    }
}