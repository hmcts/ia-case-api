package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.QUERY_MANAGEMENT_RAISE_QUERY;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isLegalRepJourney;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;

@Component
public class RaiseQueryCallbackPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseMessage> caseMessageAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public RaiseQueryCallbackPreparer(
            Appender<CaseMessage> caseMessageAppender,
            DateProvider dateProvider,
            UserDetails userDetails
    ) {
        this.caseMessageAppender = caseMessageAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == QUERY_MANAGEMENT_RAISE_QUERY;
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

        if (targetCollection != null) {
            Optional<List<IdValue<CaseQueriesCollection>>> maybeQueries =
                    asylumCase.read(targetCollection);

            if (maybeQueries.isEmpty()) {
                asylumCase.write(targetCollection, emptyList());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCaseFieldDefinition getQueryCollectionField(AsylumCase asylumCase) {
        if (isLegalRepJourney(asylumCase)) {
            return QM_LEGAL_REPRESENTATIVE_QUERIES;
        } else if (isAipJourney(asylumCase)) {
            return QM_AIP_QUERIES;
        } else if (isInternalCase(asylumCase)) {
            return QM_ADMIN_QUERIES;
        }
        return null;
    }

}

