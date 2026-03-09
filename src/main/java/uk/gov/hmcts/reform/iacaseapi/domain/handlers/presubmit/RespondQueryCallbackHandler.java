package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_ADMIN_QUERIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_LEGAL_REPRESENTATIVE_QUERIES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Optional;

@Component
public class RespondQueryCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public RespondQueryCallbackHandler(UserDetails userDetails,
                                       UserDetailsHelper userDetailsHelper) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.QUERY_MANAGEMENT_RESPOND_QUERY;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        clearOldQueryCollections(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public static boolean hasOldQueries(Callback<AsylumCase> callback) {
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<Object> adminQueries = asylumCase.read(QM_ADMIN_QUERIES);
        Optional<Object> lrQueries = asylumCase.read(QM_LEGAL_REPRESENTATIVE_QUERIES);

        return adminQueries.isPresent() || lrQueries.isPresent();
    }

    public static void clearOldQueryCollections(Callback<AsylumCase> callback) {
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (hasOldQueries(callback)) {
            asylumCase.write(QM_LEGAL_REPRESENTATIVE_QUERIES, null);
            asylumCase.write(QM_ADMIN_QUERIES, null);
        }
    }
}