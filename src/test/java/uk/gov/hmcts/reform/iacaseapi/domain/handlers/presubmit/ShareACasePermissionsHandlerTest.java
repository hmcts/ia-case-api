package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdUpdater;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalUsersRetriever;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ShareACasePermissionsHandlerTest {

    @Mock
    CcdUpdater ccdUpdater;
    @Mock
    ProfessionalUsersRetriever professionalUsersRetriever;
    private ShareACasePermissionsHandler shareACasePermissionsHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private String someUserId1 = "someUserId1";
    private String someUserEmail1 = "someUser1@example.com";
    private String someUserId2 = "someUserId2";
    private String someUserEmail2 = "someUser2@example.com";

    private ProfessionalUser professionalUserActive = new ProfessionalUser(
        someUserId1,
        "someFirstName",
        "someLastName",
        someUserEmail1,
        newArrayList(),
        "ACTIVE",
        "someCode",
        "someMessage"
    );

    private ProfessionalUser professionalUserNonActive = new ProfessionalUser(
        someUserId2,
        "someFirstName",
        "someLastName",
        someUserEmail2,
        newArrayList(),
        "NONACTIVE",
        "someCode",
        "someMessage"
    );

    private ProfessionalUsersResponse response =
        new ProfessionalUsersResponse(newArrayList(professionalUserActive, professionalUserNonActive));

    @BeforeEach
    public void setUp() throws Exception {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SHARE_A_CASE);

        DynamicList dynamicList = new DynamicList(
            new Value(someUserId1, someUserEmail1),
            newArrayList()
        );

        when(asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        when(professionalUsersRetriever.retrieve()).thenReturn(response);

        shareACasePermissionsHandler = new ShareACasePermissionsHandler(ccdUpdater, professionalUsersRetriever);

        reset(ccdUpdater);
    }

    @Test
    public void should_not_invoke_ccd_updater_when_user_is_not_valid() {

        DynamicList dynamicList = new DynamicList(
            new Value("injectedUserId", "injectedUserEmail@example.com"),
            newArrayList()
        );

        when(asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        shareACasePermissionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdUpdater, times(0)).updatePermissions(callback);
    }

    @Test
    public void should_not_invoke_ccd_updater_when_user_is_non_active() {

        DynamicList dynamicList = new DynamicList(
            new Value(someUserId2, someUserEmail2),
            newArrayList()
        );
        when(asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        shareACasePermissionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdUpdater, times(0)).updatePermissions(callback);
    }

    @Test
    public void should_invoke_ccd_updater_when_user_is_valid() {

        shareACasePermissionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdUpdater).updatePermissions(callback);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = shareACasePermissionsHandler.canHandle(callbackStage, callback);

                if (event == Event.SHARE_A_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> shareACasePermissionsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> shareACasePermissionsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
