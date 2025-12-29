package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_FCS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.CreateFlagHandler.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CreateFlagHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    private CreateFlagHandler createFlagHandler;
    private final String partyId = "party-id";
    private final String appellantNameForDisplay = "some-name";
    private final String fcsGivenName = "FcsFirstName";
    private final String fcsFamilyName = "FcsFamilyName";

    private final StrategicCaseFlag fcsCaseFlag =
        new StrategicCaseFlag(fcsGivenName + " " + fcsFamilyName, ROLE_ON_CASE_FCS);

    private final StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(appellantNameForDisplay, ROLE_ON_CASE_APPLICANT);
    private final StrategicCaseFlag strategicCaseFlagEmpty = new StrategicCaseFlag();

    private final List<PartyFlagIdValue> fcsLevelFlags = List.of(
        new PartyFlagIdValue(partyId, fcsCaseFlag));

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(APPLICANT_FULL_NAME, String.class)).thenReturn(Optional.of(appellantNameForDisplay));
        when(bailCase.read(FCS_LEVEL_FLAGS)).thenReturn(Optional.of(Collections.emptyList()));
        when(bailCase.read(FCS_N_PARTY_ID_FIELD.get(0), String.class)).thenReturn(Optional.of(partyId));

        createFlagHandler = new CreateFlagHandler();
    }

    @Test
    void should_write_to_case_flag_fields() {

        createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(bailCase, times(1))
            .write(APPELLANT_LEVEL_FLAGS, strategicCaseFlag);
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo(("Applicant"));
        verify(bailCase, times(1))
            .write(CASE_FLAGS, strategicCaseFlagEmpty);
    }

    @Test
    void should_write_to_fcs_case_flag_fields_if_has_fcs_field_yes() {
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsGivenName));
        when(bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsFamilyName));

        createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(bailCase, times(1)).write(FCS_LEVEL_FLAGS, fcsLevelFlags);
    }

    @Test
    void should_write_empty_list_to_fcs_case_flag_fields_if_has_fcs_field_is_no() {
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.of(NO));
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsGivenName));
        when(bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsFamilyName));

        createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(bailCase, times(1)).write(FCS_LEVEL_FLAGS, Collections.emptyList());
    }

    @Test
    void should_write_empty_list_to_fcs_case_flag_fields_if_has_fcs_field_is_not_set() {
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsGivenName));
        when(bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsFamilyName));

        createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(bailCase, times(1)).write(FCS_LEVEL_FLAGS, Collections.emptyList());
    }

    @Test
    void should_write_empty_list_to_fcs_case_flag_fields_if_party_id_is_not_present() {
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsGivenName));
        when(bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsFamilyName));
        when(bailCase.read(FCS_N_PARTY_ID_FIELD.get(0), String.class)).thenReturn(Optional.empty());

        createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(bailCase, times(1)).write(FCS_LEVEL_FLAGS, Collections.emptyList());
    }

    @Test
    void should_write_to_fcs_case_flag_fields_with_existing_flags() {
        List<PartyFlagIdValue> existLevelFlags = List.of(
            new PartyFlagIdValue("party-id-existing", fcsCaseFlag));

        when(bailCase.read(FCS_LEVEL_FLAGS)).thenReturn(Optional.of(existLevelFlags));
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsGivenName));
        when(bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsFamilyName));
        when(bailCase.read(FCS_N_PARTY_ID_FIELD.get(0), String.class)).thenReturn(Optional.of("party-id-existing"));

        createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(bailCase, times(1)).write(FCS_LEVEL_FLAGS, existLevelFlags);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> createFlagHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = createFlagHandler.canHandle(callbackStage, callback);

                if (event == Event.CREATE_FLAG
                    && callbackStage == ABOUT_TO_START) {
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

        assertThatThrownBy(() -> createFlagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_when_applicant_full_name_is_not_present() {
        when(bailCase.read(APPLICANT_FULL_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> createFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("applicantFullName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_fcs_given_name_is_not_present() {
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> createFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("supporterGivenNames is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_fcs_family_name_is_not_present() {
        when(bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(0), YesOrNo.class)).thenReturn(Optional.of(YES));
        when(bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(0), String.class)).thenReturn(Optional.of(fcsGivenName));
        when(bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(0), String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> createFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("supporterFamilyNames is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}

