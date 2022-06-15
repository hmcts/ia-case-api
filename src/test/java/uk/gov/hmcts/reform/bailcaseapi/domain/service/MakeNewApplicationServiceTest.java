package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_USER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_STATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRIOR_APPLICATIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS;


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
    private ObjectMapper mapper = new ObjectMapper();

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

        Mockito.when(priorApplicationAppender.append(Mockito.any(PriorApplication.class), Mockito.anyList()))
            .thenReturn(allAppendedPriorApplications);

        makeNewApplicationService.appendPriorApplication(bailCase, bailCaseBefore);

        Mockito.verify(priorApplicationAppender, Mockito.times(1)).append(
            newPriorApplicationCaptor.capture(),
            existingPriorApplicationsCaptor.capture());

        Mockito.verify(bailCase, Mockito.times(1)).write(PRIOR_APPLICATIONS, allAppendedPriorApplications);
    }


    @Test
    void should_remove_fields_not_in_list() {
        BailCase bailCase = new BailCase();
        bailCase.write(CURRENT_USER, "current_user");
        bailCase.write(OUTCOME_STATE, "applicationEnded");

        Mockito.when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        makeNewApplicationService.clearUnrelatedFields(bailCase);
        assertThat(bailCase).isEmpty();
    }

    @Test
    void should_remove_if_value_is_null() {
        BailCase bailCase = new BailCase();
        bailCase.write(CURRENT_USER, null);
        bailCase.write(OUTCOME_STATE, null);

        Mockito.when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        makeNewApplicationService.clearUnrelatedFields(bailCase);
        assertThat(bailCase).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleLabel.class, names = {"LEGAL_REPRESENTATIVE", "HOME_OFFICE_GENERIC"})
    void should_clear_role_dependent_field(UserRoleLabel userRoleLabel) {
        List<IdValue<DocumentWithDescription>> b1DocumentList =
            Arrays.asList(
                new IdValue<>("1", b1Document)
            );

        bailCase.write(UPLOAD_B1_FORM_DOCS, b1DocumentList);

        Mockito.when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(userRoleLabel);

        makeNewApplicationService.clearUnrelatedFields(bailCase);
        Mockito.verify(bailCase, Mockito.times(1)).remove(UPLOAD_B1_FORM_DOCS);
    }

    @Test
    void should_not_clear_role_dependent_field_if_admin() {
        List<IdValue<DocumentWithDescription>> b1DocumentList =
            Arrays.asList(
                new IdValue<>("1", b1Document)
            );

        bailCase.write(UPLOAD_B1_FORM_DOCS, b1DocumentList);

        Mockito.when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        makeNewApplicationService.clearUnrelatedFields(bailCase);
        Mockito.verify(bailCase, Mockito.times(0)).clear(UPLOAD_B1_FORM_DOCS);
    }

    @Test
    void should_convert_checked_exception_to_runtime_on_error() throws JsonProcessingException {

        Mockito.doThrow(Mockito.mock(JsonProcessingException.class))
            .when(mapper)
            .writeValueAsString(bailCaseBefore);

        assertThatThrownBy(() -> makeNewApplicationService.appendPriorApplication(bailCase, bailCaseBefore))
            .hasMessage("Could not serialize data")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
