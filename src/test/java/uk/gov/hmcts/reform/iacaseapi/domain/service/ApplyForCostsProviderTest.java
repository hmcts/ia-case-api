package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private String applyForCostsOotExplanation = "Test explanation";
    private List<IdValue<Document>> evidence =
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

        List<Value> applyForCostsForRespondent = applyForCostsProvider.getApplyForCosts(asylumCase);
        assertNotNull(applyForCostsForRespondent);
        assertThat(applyForCostsForRespondent.size()).isEqualTo(1);
        assertThat(applyForCostsForRespondent.get(0).getLabel().equals("Costs 1, Wasted Costs, 10 Nov 2023"));
    }

    @Test
    void should_return_empty_list_for_HO_loggedIn() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.HOME_OFFICE_GENERIC);

        List<Value> applyForCostsForRespondent = applyForCostsProvider.getApplyForCosts(asylumCase);
        assertNotNull(applyForCostsForRespondent);
        assertThat(applyForCostsForRespondent.size()).isEqualTo(0);
    }
}