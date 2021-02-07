package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@Component
public class HomeOfficeCaseNotificationsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    private static final String HO_NOTIFICATION_FEATURE = "home-office-notification-feature";

    public HomeOfficeCaseNotificationsHandler(
        FeatureToggler featureToggler,
        HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.featureToggler = featureToggler;
        this.homeOfficeApi = homeOfficeApi;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (Arrays.asList(
                    Event.REQUEST_RESPONDENT_EVIDENCE,
                    Event.REQUEST_RESPONDENT_REVIEW,
                    Event.LIST_CASE,
                    Event.EDIT_CASE_LISTING,
                    Event.ADJOURN_HEARING_WITHOUT_DATE,
                    Event.SEND_DECISION_AND_REASONS,
                    Event.APPLY_FOR_FTPA_APPELLANT,
                    Event.APPLY_FOR_FTPA_RESPONDENT,
                    Event.LEADERSHIP_JUDGE_FTPA_DECISION,
                    Event.RESIDENT_JUDGE_FTPA_DECISION,
                    Event.END_APPEAL,
                    Event.REQUEST_RESPONSE_AMEND
                ).contains(callback.getEvent())
               || (callback.getEvent() == Event.SEND_DIRECTION
                   && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE
                   && getLatestNonStandardRespondentDirection(
                        callback.getCaseDetails().getCaseData()).isPresent())

               || (callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                   && (Arrays.asList(
                        State.AWAITING_RESPONDENT_EVIDENCE,
                        State.RESPONDENT_REVIEW
                        ).contains(callback.getCaseDetails().getState()))
                   && isDirectionForRespondentParties(callback.getCaseDetails().getCaseData())
                  )
               )
               && featureToggler.getValue(HO_NOTIFICATION_FEATURE, false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithHomeOfficeData = homeOfficeApi.call(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
    }

    protected Optional<Direction> getLatestNonStandardRespondentDirection(AsylumCase asylumCase) {

        Optional<List<IdValue<Direction>>> maybeExistingDirections = asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS);

        return maybeExistingDirections
            .orElseThrow(() -> new IllegalStateException("directions not present"))
            .stream()
            .max(Comparator.comparingInt(s -> Integer.parseInt(s.getId())))
            .filter(idValue -> idValue.getValue().getTag().equals(DirectionTag.NONE))
            .filter(idValue -> idValue.getValue().getParties().equals(Parties.RESPONDENT))
            .map(IdValue::getValue);
    }

    protected boolean isDirectionForRespondentParties(AsylumCase asylumCase) {

        String parties = asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_EDIT_PARTIES, String.class).orElse("");
        return Parties.RESPONDENT.toString().equals(parties);


    }
}
