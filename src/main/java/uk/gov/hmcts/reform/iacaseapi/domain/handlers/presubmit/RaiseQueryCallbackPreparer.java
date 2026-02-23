package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;

@Component
public class RaiseQueryCallbackPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;

    public RaiseQueryCallbackPreparer(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.QUERY_MANAGEMENT_RAISE_QUERY;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AsylumCaseFieldDefinition targetCollection = getQueryCollectionField(asylumCase);

        if (targetCollection == null) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        Optional<List<IdValue<CaseQueriesCollection>>> maybeQueries =
                asylumCase.read(targetCollection);

        List<IdValue<CaseQueriesCollection>> queries =
                maybeQueries.orElse(emptyList());

        if (maybeQueries.isEmpty()) {
            asylumCase.write(targetCollection, queries);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCaseFieldDefinition getQueryCollectionField(AsylumCase asylumCase) {
        if (uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney(asylumCase)) {
            return QM_LEGAL_REPRESENTATIVE_QUERIES;
        } else if (uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney(asylumCase)) {
            return QM_AIP_QUERIES;
        } else if (uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase(asylumCase)) {
            return QM_ADMIN_QUERIES;
        }
        return null;
    }
}
