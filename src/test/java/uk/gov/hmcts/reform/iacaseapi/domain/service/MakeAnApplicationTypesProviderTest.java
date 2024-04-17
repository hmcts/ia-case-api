package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.ADJOURN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.CHANGE_HEARING_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.EXPEDITE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.JUDGE_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.LINK_OR_UNLINK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.REINSTATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.TIME_EXTENSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.TRANSFER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.UPDATE_APPEAL_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.WITHDRAW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_TAKEN_OFFLINE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.ENDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MakeAnApplicationTypesProviderTest {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseCaseDetails;

    @Mock UserDetails userDetails;

    @InjectMocks
    private MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    @Test
    void should_return_given_application_types_in_appeal_submitted_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(APPEAL_SUBMITTED);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()),
            new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    void should_return_given_application_types_in_decided_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(DECIDED);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
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
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()),
            new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "FTPA_SUBMITTED",
        "FTPA_DECIDED"
    })
    void should_return_valid_application_types_in_ftpa_states(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(state);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "ADJOURNED", "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION"
    })
    void should_return_given_application_types_in_adjourned_to_decision(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
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
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()),
            new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    void should_return_given_application_types_in_ended_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(ENDED);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(REINSTATE.name(), REINSTATE.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    void should_return_given_application_types_in_final_bundling() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(FINAL_BUNDLING);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    void should_return_given_application_types_in_listing() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(LISTING);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
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
