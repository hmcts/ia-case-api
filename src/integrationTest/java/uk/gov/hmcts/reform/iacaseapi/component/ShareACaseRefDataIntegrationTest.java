package uk.gov.hmcts.reform.iacaseapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SHARE_A_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.RefDataIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.PreSubmitCallbackResponseForTest;

public class ShareACaseRefDataIntegrationTest extends RefDataIntegrationTest {

    @Test
    public void should_get_users_from_professional_ref_data() {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-legalrep-solicitor")));

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToStart(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        AsylumCase asylumCase = response.getAsylumCase();

        Optional<DynamicList> listOfUsers = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        assertThat(listOfUsers.isPresent()).as("No colleagueId returned").isTrue();

        DynamicList dynamicList = listOfUsers.orElseThrow(() -> new IllegalStateException("no users returned"));

        List<Value> listItems = dynamicList.getListItems();
        assertThat(listItems.size()).isEqualTo(2);

        assertThat(listItems.get(0).getCode()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(0).getUserIdentifier());
        assertThat(listItems.get(1).getCode()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(1).getUserIdentifier());
        assertThat(listItems.get(0).getLabel()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(0).getEmail());
        assertThat(listItems.get(1).getLabel()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(1).getEmail());

    }

}
