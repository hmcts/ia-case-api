package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CreateFlagConfirmationTest {

    private String appellantNameForDisplay = "some-name";
    private String roleOnCase = "some-role";

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CreateFlagConfirmation createFlagConfirmation =
            new CreateFlagConfirmation(ccdSupplementaryUpdater, roleOnCase);

    @BeforeEach
    public void setUp() {
        createFlagConfirmation = new CreateFlagConfirmation(ccdSupplementaryUpdater,
                roleOnCase
        );

    }

    @Test
    void should_invoke_supplementary_updater() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.ofNullable(appellantNameForDisplay));


        createFlagConfirmation.handle(callback);

        Map<String, Object> coreData = Maps.newHashMap();
        coreData.put("partyName", appellantNameForDisplay);
        coreData.put("roleOnCase", roleOnCase);

        verify(ccdSupplementaryUpdater).setSupplementaryValues(callback, coreData);
    }

    @Test
    void should_return_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.ofNullable(appellantNameForDisplay));

        PostSubmitCallbackResponse callbackResponse =
            createFlagConfirmation.handle(callback);

        assertNotNull(callbackResponse);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> createFlagConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = createFlagConfirmation.canHandle(callback);

            if (event == Event.CREATE_FLAG) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> createFlagConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
