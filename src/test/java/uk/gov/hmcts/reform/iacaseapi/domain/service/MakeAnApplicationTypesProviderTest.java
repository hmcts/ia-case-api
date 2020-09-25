package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unchecked")
public class MakeAnApplicationTypesProviderTest {

    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseCaseDetails;

    @Mock private UserDetails userDetails;
    @Mock private UserDetailsProvider userDetailsProvider;

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";

    @InjectMocks
    private MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void should_return_given_application_types_in_appeal_submitted_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(APPEAL_SUBMITTED);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    public void should_return_given_application_types_in_decided_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(DECIDED);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

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

    @Test
    @Parameters({
        "PENDING_PAYMENT",
        "AWAITING_RESPONDENT_EVIDENCE",
        "CASE_BUILDING",
        "CASE_UNDER_REVIEW",
        "RESPONDENT_REVIEW",
        "SUBMIT_HEARING_REQUIREMENTS"
    })
    public void should_return_given_application_types_in_pending_payment_case_building(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    @Parameters({
        "FTPA_SUBMITTED",
        "FTPA_DECIDED"
    })
    public void should_return_valid_application_types_in_ftpa_states(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

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

    @Test
    @Parameters({ "ADJOURNED", "PREPARE_FOR_HEARING", "PRE_HEARING", "DECISION" })
    public void should_return_given_application_types_in_adjourned_to_decision(State state) {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

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
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    public void should_return_given_application_types_in_ended_state() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(ENDED);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

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
    public void should_return_given_application_types_in_final_bundling() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(FINAL_BUNDLING);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    public void should_return_given_application_types_in_listing() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(ROLE_LEGAL_REP));

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(LISTING);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()),
            new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
        DynamicList actualList =
            new DynamicList(values.get(0), values);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);
        assertThat(expectedList).isEqualTo(actualList);
    }

    @Test
    public void should_return_null_invalid_state() {

        when(callback.getCaseDetails()).thenReturn(caseCaseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(APPEAL_TAKEN_OFFLINE);
        when(userDetailsProvider.getUserDetails())
            .thenReturn(userDetails);

        DynamicList expectedList = makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback);
        assertNotNull(expectedList);

        List<Value> values = expectedList.getListItems();
        assertThatThrownBy(() -> values.get(0))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
