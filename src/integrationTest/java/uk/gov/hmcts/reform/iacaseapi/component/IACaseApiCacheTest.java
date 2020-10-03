package uk.gov.hmcts.reform.iacaseapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

public class IACaseApiCacheTest extends SpringBootIntegrationTest {

    private UserDetails caseWorkerDetails;
    private UserDetails judgeDetails;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_case_worker_details_from_cache() {

        given.someLoggedIn(userWith()
            .email("caseworker@test.com")
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer"));


        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(ADD_CASE_NOTE)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(ADD_CASE_NOTE_SUBJECT, "some-subject")
                    .with(ADD_CASE_NOTE_DESCRIPTION, "some-description")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Cache cache = (Cache) cacheManager.getCache("IdamUserDetails").getNativeCache();
        ConcurrentMap<Object, Object> cacheMap = cache.asMap();
        Set<Object> keys = cacheMap.keySet();
        keys.stream().forEach(k -> {
            UserDetails userDetails = (UserDetails)  cacheMap.get(k);
            caseWorkerDetails = userDetails;
        });

        assertNotNull(caseWorkerDetails);
        assertThat(caseWorkerDetails.getId()).isEqualTo("1");
        assertThat(caseWorkerDetails.getEmailAddress()).isEqualTo("caseworker@test.com");
        assertThat(caseWorkerDetails.getForename()).isEqualTo("Case");
        assertThat(caseWorkerDetails.getSurname()).isEqualTo("Officer");
        assertThat(caseWorkerDetails.getRoles()).isEqualTo(Arrays.asList("caseworker-ia", "caseworker-ia-caseofficer"));
    }


    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-iacjudge"})
    public void should_return_judge_details_from_cache() {

        given.someLoggedIn(userWith()
            .email("judge@test.com")
            .roles(newHashSet("caseworker-ia", "caseworker-ia-iacjudge"))
            .forename("IAC")
            .surname("Judge"));

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(ADD_CASE_NOTE)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(ADD_CASE_NOTE_SUBJECT, "some-subject")
                    .with(ADD_CASE_NOTE_DESCRIPTION, "some-description")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        Cache cache = (Cache) cacheManager.getCache("IdamUserDetails").getNativeCache();
        ConcurrentMap<Object, Object> cacheMap = cache.asMap();
        Set<Object> keys = cacheMap.keySet();
        keys.stream().forEach(k -> {
            UserDetails userDetails = (UserDetails)  cacheMap.get(k);
            judgeDetails = userDetails;
        });

        assertNotNull(judgeDetails);
        assertThat(judgeDetails.getId()).isEqualTo("1");
        assertThat(judgeDetails.getEmailAddress()).isEqualTo("judge@test.com");
        assertThat(judgeDetails.getForename()).isEqualTo("IAC");
        assertThat(judgeDetails.getSurname()).isEqualTo("Judge");
        assertThat(judgeDetails.getRoles()).isEqualTo(Arrays.asList("caseworker-ia", "caseworker-ia-iacjudge"));
    }
}
