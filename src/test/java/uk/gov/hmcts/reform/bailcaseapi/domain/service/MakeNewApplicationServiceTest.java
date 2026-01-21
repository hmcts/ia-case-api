package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_USER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PRIOR_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ListingHearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;


@ExtendWith(MockitoExtension.class)
class MakeNewApplicationServiceTest {

    @Mock
    private BailCase bailCase;
    @Mock
    private BailCase bailCaseBefore;
    @Mock
    private List<IdValue<PriorApplication>> allAppendedPriorApplications;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    @Mock
    private Appender<PriorApplication> priorApplicationAppender;
    @Mock
    private DocumentWithDescription b1Document;

    private MakeNewApplicationService makeNewApplicationService;

    @Mock
    private final ObjectMapper mapper = new ObjectMapper();

    @Captor
    private ArgumentCaptor<List<IdValue<PriorApplication>>> existingPriorApplicationsCaptor;
    @Captor private ArgumentCaptor<PriorApplication> newPriorApplicationCaptor;

    @BeforeEach
    public void setup() {
        makeNewApplicationService =
            new MakeNewApplicationService(priorApplicationAppender, userDetails, userDetailsHelper, mapper);
    }

    @Test
    void should_append_prior_application_to_empty_list() {
        // given
        when(priorApplicationAppender.append(Mockito.any(PriorApplication.class), Mockito.anyList()))
            .thenReturn(allAppendedPriorApplications);

        // when
        makeNewApplicationService.appendPriorApplication(bailCase, bailCaseBefore);

        // then
        verify(priorApplicationAppender, Mockito.times(1)).append(
            newPriorApplicationCaptor.capture(),
            existingPriorApplicationsCaptor.capture());

        verify(bailCase, Mockito.times(1)).write(PRIOR_APPLICATIONS, allAppendedPriorApplications);
    }


    @Test
    void should_remove_fields_not_in_list_about_to_start() {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(CURRENT_USER, "current_user");
        bailCase.write(OUTCOME_STATE, "applicationEnded");

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToStart(bailCase);

        // then
        assertThat(bailCase).isEmpty();
    }

    @Test
    void should_remove_fields_not_in_list_about_to_submit() {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(CURRENT_USER, "current_user");
        bailCase.write(OUTCOME_STATE, "applicationEnded");

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        // then
        assertThat(bailCase).isEmpty();
    }

    @Test
    void should_remove_if_value_is_null() {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(CURRENT_USER, null);
        bailCase.write(OUTCOME_STATE, null);

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        // then
        assertThat(bailCase).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleLabel.class, names = {"LEGAL_REPRESENTATIVE", "HOME_OFFICE_BAIL"})
    void should_clear_role_dependent_field(UserRoleLabel userRoleLabel) {
        // given
        List<IdValue<DocumentWithDescription>> b1DocumentList =
            singletonList(
                new IdValue<>("1", b1Document)
            );

        bailCase.write(UPLOAD_B1_FORM_DOCS, b1DocumentList);

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(userRoleLabel);

        // when
        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        // then
        verify(bailCase, Mockito.times(1)).remove(UPLOAD_B1_FORM_DOCS);
    }

    @Test
    void should_not_clear_role_dependent_field_if_admin() {
        // given
        List<IdValue<DocumentWithDescription>> b1DocumentList =
            asList(
                new IdValue<>("1", b1Document)
            );

        bailCase.write(UPLOAD_B1_FORM_DOCS, b1DocumentList);

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        // then
        verify(bailCase, Mockito.times(0)).clear(UPLOAD_B1_FORM_DOCS);
    }

    @Test
    void should_convert_checked_exception_to_runtime_on_error() throws JsonProcessingException {

        doThrow(Mockito.mock(JsonProcessingException.class))
            .when(mapper)
            .writeValueAsString(bailCaseBefore);

        assertThatThrownBy(() -> makeNewApplicationService.appendPriorApplication(bailCase, bailCaseBefore))
            .hasMessage("Could not serialize data")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_convert_string_to_bail_case() throws JsonProcessingException {
        // given
        String caseDataJson = "{\"endApplicationOutcome\":\"Bail dismissed without a hearing\","
            + "\"applicantGivenNames\":\"John\",\"applicantFamilyName\":\"Smith\","
            + "\"outcomeState\":\"applicationEnded\",\"endApplicationDate\":\"2022-06-20\"}";
        when(mapper.readValue(caseDataJson, BailCase.class)).thenReturn(bailCase);

        // then
        BailCase bailCase = makeNewApplicationService.getBailCaseFromString(caseDataJson);

        // then
        assertNotNull(bailCase);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void should_throw_exception_for_empty_null_json(String json) {
        assertThatThrownBy(() -> makeNewApplicationService.getBailCaseFromString(json))
            .hasMessage("CaseData (json) is missing")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_keep_case_notes_about_to_start() {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(CASE_NOTES, "case note");

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToStart(bailCase);

        // then
        assertTrue(bailCase.containsKey(CASE_NOTES.value()));
    }

    @Test
    void should_keep_case_notes_about_to_submit() {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(CASE_NOTES, "case note");

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        // then
        assertTrue(bailCase.containsKey(CASE_NOTES.value()));
    }

    @Test
    void should_keep_hearing_details_about_to_submit() {
        // given
        BailCase bailCase = new BailCase();
        bailCase.write(LISTING_LOCATION, ListingHearingCentre.BIRMINGHAM);
        bailCase.write(LIST_CASE_HEARING_DATE, "2024-04-04T08:00:00.000");
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        // when
        makeNewApplicationService.clearFieldsAboutToSubmit(bailCase);

        // then
        assertTrue(bailCase.containsKey(LISTING_LOCATION.value()));
        assertTrue(bailCase.containsKey(LIST_CASE_HEARING_DATE.value()));
    }
}
