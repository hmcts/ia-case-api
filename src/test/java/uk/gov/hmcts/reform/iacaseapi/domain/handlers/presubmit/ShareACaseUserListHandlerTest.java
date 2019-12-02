package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ORG_LIST_OF_USERS;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ShareACaseUserListHandlerTest {

    private ShareACaseUserListHandler shareACaseUserListHandler;

    @Mock ProfessionalUsersRetriever professionalUsersRetriever;
    @Mock ProfessionalUsersResponse professionalUsersResponse;
    @Mock ProfessionalUser professionalUser;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;


    @Before
    public void setUp() throws Exception {
        shareACaseUserListHandler = new ShareACaseUserListHandler(
            professionalUsersRetriever
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SHARE_A_CASE);

    }

    @Test
    public void should_respond_with_asylum_case_with_dynamic_list_with_results() {

        String userId = "12345";
        String userEmail = "some-email@somewhere.com";

        ArgumentCaptor<DynamicList> captor = ArgumentCaptor.forClass(DynamicList.class);
        List<ProfessionalUser> professionalUsers = Lists.newArrayList(professionalUser);

        when(professionalUsersRetriever.retrieve()).thenReturn(professionalUsersResponse);
        when(professionalUsersResponse.getProfessionalUsers()).thenReturn(professionalUsers);
        when(professionalUser.getUserIdentifier()).thenReturn(userId);
        when(professionalUser.getEmail()).thenReturn(userEmail);

        PreSubmitCallbackResponse<AsylumCase> response =
            shareACaseUserListHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(AsylumCase.class);

        verify(asylumCase).clear(ORG_LIST_OF_USERS);
        verify(asylumCase).write(eq(ORG_LIST_OF_USERS), captor.capture());
        DynamicList dynamicList = captor.getValue();

        List<Value> values = dynamicList.getListItems();
        assertThat(dynamicList.getValue().getCode()).isEqualTo(userId);
        assertThat(dynamicList.getValue().getLabel()).isEqualTo(userEmail);
        assertThat(values.size()).isEqualTo(1);
        assertThat(values.get(0).getCode()).isEqualTo(userId);
        assertThat(values.get(0).getLabel()).isEqualTo(userEmail);

    }

    @Test
    public void should_respond_with_asylum_case_with_empty_dynamic_list() {

        ArgumentCaptor<DynamicList> captor = ArgumentCaptor.forClass(DynamicList.class);
        List<ProfessionalUser> professionalUsers = Lists.newArrayList();

        when(professionalUsersRetriever.retrieve()).thenReturn(professionalUsersResponse);
        when(professionalUsersResponse.getProfessionalUsers()).thenReturn(professionalUsers);

        PreSubmitCallbackResponse<AsylumCase> response =
            shareACaseUserListHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(AsylumCase.class);

        verify(asylumCase).clear(ORG_LIST_OF_USERS);
        verify(asylumCase).write(eq(ORG_LIST_OF_USERS), captor.capture());
        DynamicList dynamicList = captor.getValue();
        assertThat(dynamicList.getListItems()).isNull();

    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = shareACaseUserListHandler.canHandle(callbackStage, callback);

                if (event == Event.SHARE_A_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }


    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> shareACaseUserListHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> shareACaseUserListHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> shareACaseUserListHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> shareACaseUserListHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
