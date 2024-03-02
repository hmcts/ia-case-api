package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.launchdarkly.sdk.LDValue;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

class RecordAttendeesAndDurationTest extends SpringBootIntegrationTest implements WithServiceAuthStub {

    @MockBean
    UserDetailsProvider userDetailsProvider;

    @Mock
    private FeatureToggler featureToggler;

    @Mock
    private UserDetails userDetails;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void sets_flag_to_indicate_the_hearing_details_have_been_recorded() {

        addServiceAuthStub(server);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(Event.RECORD_ATTENDEES_AND_DURATION)
            .caseDetails(someCaseDetailsWith()
                .state(State.DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Optional<YesOrNo> hearingDetailsRecordedFlag =
            response.getAsylumCase().read(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);

        assertThat(hearingDetailsRecordedFlag.get()).isEqualTo(YES);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void returns_confirmation_page_content() {

        LDValue defaultValue = LDValue.parse("{\"epimsIds\":[]}");

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(featureToggler.getJsonValue("auto-hearing-request-locations-list", defaultValue))
            .thenReturn(defaultValue);

        addServiceAuthStub(server);
        PostSubmitCallbackResponseForTest response = iaCaseApiClient.ccdSubmitted(callback()
            .event(Event.RECORD_ATTENDEES_AND_DURATION)
            .caseDetails(someCaseDetailsWith()
                .state(State.DECISION)
                .caseData(anAsylumCase())));

        assertThat(response.getConfirmationHeader().get())
            .isEqualTo("# You have recorded the attendees and duration of the hearing");
        assertThat(response.getConfirmationBody().get()).contains("You don't need to do anything more with this case.");
    }
}
