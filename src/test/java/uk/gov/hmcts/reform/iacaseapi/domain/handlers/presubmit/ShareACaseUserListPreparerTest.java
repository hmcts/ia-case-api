package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ORG_LIST_OF_USERS;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ShareACaseUserListPreparerTest {

    ShareACaseUserListPreparer shareACaseUserListPreparer;

    @Mock private ProfessionalUsersRetriever professionalUsersRetriever;
    @Mock private ProfessionalUsersResponse professionalUsersResponse;
    @Mock private ProfessionalUser professionalUser;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;


    @BeforeEach
    void setUp() throws Exception {

        shareACaseUserListPreparer = new ShareACaseUserListPreparer(
            professionalUsersRetriever
        );
    }

    @Test
    void should_respond_with_asylum_case_with_dynamic_list_with_results() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SHARE_A_CASE);

        String userId = "12345";
        String userEmail = "some-email@somewhere.com";

        ArgumentCaptor<DynamicList> captor = ArgumentCaptor.forClass(DynamicList.class);
        List<ProfessionalUser> professionalUsers = Lists.newArrayList(professionalUser);

        when(professionalUsersRetriever.retrieve()).thenReturn(professionalUsersResponse);
        when(professionalUsersResponse.getUsers()).thenReturn(professionalUsers);
        when(professionalUser.getUserIdentifier()).thenReturn(userId);
        when(professionalUser.getEmail()).thenReturn(userEmail);
        when(professionalUser.getIdamStatus()).thenReturn("ACTIVE");

        PreSubmitCallbackResponse<AsylumCase> response =
            shareACaseUserListPreparer.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(AsylumCase.class);

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
    void should_respond_with_asylum_case_with_empty_dynamic_list() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SHARE_A_CASE);

        ArgumentCaptor<DynamicList> captor = ArgumentCaptor.forClass(DynamicList.class);
        List<ProfessionalUser> professionalUsers = Lists.newArrayList();

        when(professionalUsersRetriever.retrieve()).thenReturn(professionalUsersResponse);
        when(professionalUsersResponse.getUsers()).thenReturn(professionalUsers);

        PreSubmitCallbackResponse<AsylumCase> response =
            shareACaseUserListPreparer.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(AsylumCase.class);

        verify(asylumCase).write(eq(ORG_LIST_OF_USERS), captor.capture());
        DynamicList dynamicList = captor.getValue();
        assertThat(dynamicList.getListItems()).isNull();

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = shareACaseUserListPreparer.canHandle(callbackStage, callback);

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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> shareACaseUserListPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> shareACaseUserListPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
