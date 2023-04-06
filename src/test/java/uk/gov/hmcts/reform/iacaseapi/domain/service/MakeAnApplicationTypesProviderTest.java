package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_TO_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.ADJOURN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.EXPEDITE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.JUDGE_REVIEW_LO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.LINK_OR_UNLINK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.REINSTATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.TIME_EXTENSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.TRANSFER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.UPDATE_APPEAL_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.WITHDRAW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_TAKEN_OFFLINE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.ENDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PENDING_PAYMENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MakeAnApplicationTypesProviderTest {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";
    private static final String ROLE_ADMIN = "caseworker-ia-admofficer";
    private static final String ROLE_HO_RESPONDENT = "caseworker-ia-respondentofficer";

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseCaseDetails;
    @Mock AsylumCase asylumCase;

    @Mock UserDetails userDetails;

    @InjectMocks
    private MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    @Test
    void should_return_given_application_types_in_appeal_submitted_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(APPEAL_SUBMITTED);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @Test
    void should_return_given_application_types_in_decided_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(DECIDED);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PENDING_PAYMENT",
        "AWAITING_RESPONDENT_EVIDENCE",
        "CASE_BUILDING",
        "AWAITING_REASONS_FOR_APPEAL",
        "AWAITING_CLARIFYING_QUESTIONS_ANSWERS",
        "AWAITING_CMA_REQUIREMENTS",
        "CASE_UNDER_REVIEW",
        "REASONS_FOR_APPEAL_SUBMITTED",
        "RESPONDENT_REVIEW",
        "SUBMIT_HEARING_REQUIREMENTS"
    })
    void should_return_given_application_types_in_pending_payment_case_building(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(OTHER.name(), OTHER.toString()));

        if (!state.equals(PENDING_PAYMENT)) {
            values.addAll(Arrays.asList(
                new Value(ADJOURN.name(), ADJOURN.toString()),
                new Value(EXPEDITE.name(), EXPEDITE.toString()),
                new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                    TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
                new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString())
            ));
        }

        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "AWAITING_RESPONDENT_EVIDENCE",
        "CASE_BUILDING",
        "AWAITING_REASONS_FOR_APPEAL",
        "AWAITING_CLARIFYING_QUESTIONS_ANSWERS",
        "AWAITING_CMA_REQUIREMENTS",
        "CASE_UNDER_REVIEW",
        "REASONS_FOR_APPEAL_SUBMITTED",
        "RESPONDENT_REVIEW",
        "SUBMIT_HEARING_REQUIREMENTS",
        "ADJOURNED",
        "PREPARE_FOR_HEARING",
        "PRE_HEARING",
        "DECISION"
    })
    void should_return_correct_application_types_when_internal_ada_case(State state) {
        // For internal case, ADA, between AWAITING_RESPONDENT_EVIDENCE and DECIDED

        when(userDetails.getRoles()).thenReturn(List.of(ROLE_ADMIN));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(ADJOURN.name(), ADJOURN.toString()),
            new Value(EXPEDITE.name(), EXPEDITE.toString()),
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(OTHER.name(), OTHER.toString()),
            new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                    TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()));

        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "AWAITING_RESPONDENT_EVIDENCE",
        "CASE_BUILDING",
        "AWAITING_REASONS_FOR_APPEAL",
        "AWAITING_CLARIFYING_QUESTIONS_ANSWERS",
        "AWAITING_CMA_REQUIREMENTS",
        "CASE_UNDER_REVIEW",
        "REASONS_FOR_APPEAL_SUBMITTED",
        "RESPONDENT_REVIEW",
        "SUBMIT_HEARING_REQUIREMENTS",
        "FTPA_SUBMITTED",
        "FTPA_DECIDED",
        "ADJOURNED",
        "PREPARE_FOR_HEARING",
        "PRE_HEARING",
        "DECISION"
    })
    void should_have_updateHearingRequirements_when_internal_ada_case_after_hearing_req_submitted(State state) {
        // For internal case, ADA, in AWAITING_RESPONDENT_EVIDENCE state (after triggering SUBMIT_HEARING_REQUIREMENT)

        // The state doesn't change for an ADA case after SUBMIT_HEARING_REQUIREMENTS (remains
        // AWAITING_RESPONDENT_EVIDENCE) but ADA_HEARING_REQUIREMENTS_TO_REVIEW will be set to YES

        when(userDetails.getRoles()).thenReturn(List.of(ROLE_ADMIN));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_TO_REVIEW, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems())
            .contains(new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "FTPA_SUBMITTED",
        "FTPA_DECIDED"
    })
    void should_return_valid_application_types_in_ftpa_states(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "ADJOURNED", "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION"
    })
    void should_return_given_application_types_in_adjourned_to_decision_non_ada(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(ADJOURN.name(), ADJOURN.toString()),
            new Value(EXPEDITE.name(), EXPEDITE.toString()),
            new Value(TRANSFER.name(), TRANSFER.toString()),
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "ADJOURNED", "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION"
    })
    void should_return_given_application_types_in_adjourned_to_decision_ada(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(ADJOURN.name(), ADJOURN.toString()),
            new Value(EXPEDITE.name(), EXPEDITE.toString()),
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @Test
    void should_return_given_application_types_in_ended_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(ENDED);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(REINSTATE.name(), REINSTATE.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @Test
    void should_return_given_application_types_in_final_bundling() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(FINAL_BUNDLING);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(ADJOURN.name(), ADJOURN.toString()),
            new Value(EXPEDITE.name(), EXPEDITE.toString()),
            new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @Test
    void should_return_given_application_types_in_listing_non_ada() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(LISTING);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }

    @Test
    void should_return_given_application_types_in_listing_ada() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(LISTING);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()),
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(ADJOURN.name(), ADJOURN.toString()),
            new Value(EXPEDITE.name(), EXPEDITE.toString()),
            new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList.getListItems()).containsAll(actualList.getListItems());
    }


    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION"
    })
    void should_return_transfer_types_when_state_from_hearing_to_decision_with_non_ada_in_ho_role(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_HO_RESPONDENT));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
                new Value(TRANSFER.name(), TRANSFER.toString()));
        DynamicList expectedList =
                new DynamicList(values.get(0), values);

        DynamicList actualList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(actualList);
        assertThat(actualList.getListItems()).containsAll(expectedList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION"
    })
    void should_not_return_transfer_types_when_state_from_hearing_to_decision_with_ada_in_ho_role(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_HO_RESPONDENT));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
                new Value(TRANSFER.name(), TRANSFER.toString()));
        DynamicList expectedList =
                new DynamicList(values.get(0), values);

        DynamicList actualList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(actualList);
        assertThat(actualList.getListItems()).doesNotContainAnyElementsOf(expectedList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION"
    })
    void should_return_adjourn_expedite_types_after_listing_state_with_non_ada_in_ho_role(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_HO_RESPONDENT));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
                new Value(ADJOURN.name(), ADJOURN.toString()),
                new Value(EXPEDITE.name(), EXPEDITE.toString())
        );
        DynamicList expectedList =
                new DynamicList(values.get(0), values);

        DynamicList actualList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(actualList);
        assertThat(actualList.getListItems()).containsAll(expectedList.getListItems());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "AWAITING_RESPONDENT_EVIDENCE",
        "CASE_BUILDING",
        "CASE_UNDER_REVIEW",
        "RESPONDENT_REVIEW",
        "SUBMIT_HEARING_REQUIREMENTS",
        "LISTING",
        "PREPARE_FOR_HEARING",
        "FINAL_BUNDLING",
        "PRE_HEARING",
        "DECISION"
    })
    void should_return_adjourn_expedite_types_when_state_from_evidence_to_decision_with_ada_in_ho_role(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_HO_RESPONDENT));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(caseCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
                new Value(ADJOURN.name(), ADJOURN.toString()),
                new Value(EXPEDITE.name(), EXPEDITE.toString())
        );
        DynamicList expectedList =
                new DynamicList(values.get(0), values);

        DynamicList actualList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(actualList);
        assertThat(actualList.getListItems()).containsAll(expectedList.getListItems());
    }

    @Test
    void should_return_null_invalid_state() {

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(APPEAL_TAKEN_OFFLINE);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);

        List<Value> values = expectedList.getListItems();
        assertThatThrownBy(() -> values.get(0))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
