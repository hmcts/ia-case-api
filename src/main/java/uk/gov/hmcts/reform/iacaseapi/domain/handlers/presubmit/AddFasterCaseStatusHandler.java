package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FasterCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_FASTER_CASE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Component
public class AddFasterCaseStatusHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<FasterCaseStatus> fasterCaseStatusAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public AddFasterCaseStatusHandler(
        Appender<FasterCaseStatus> fasterCaseStatusAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.fasterCaseStatusAppender = fasterCaseStatusAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(ADD_FASTER_CASE_STATUS);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Boolean fasterCaseStatus = asylumCase
                .read(FASTER_CASE_STATUS, YesOrNo.class)
                .map(fasterCaseStatusValue -> YES == fasterCaseStatusValue)
                .orElseThrow(() -> new IllegalStateException("fasterCaseStatus is not present"));

        String fasterCaseStatusReason = asylumCase
                .read(FASTER_CASE_STATUS_REASON, String.class)
                .orElseThrow(() -> new IllegalStateException("fasterCaseStatusReason is not present"));

        Optional<List<IdValue<FasterCaseStatus>>> maybeExistingFasterCaseStatuses =
            asylumCase.read(FASTER_CASE_STATUSES);

        final FasterCaseStatus newFasterCaseStatus = new FasterCaseStatus(
            fasterCaseStatus,
            fasterCaseStatusReason,
            buildFullName(),
            dateProvider.now().toString()
        );

        List<IdValue<FasterCaseStatus>> allFasterCaseStatuses =
            fasterCaseStatusAppender.append(newFasterCaseStatus, maybeExistingFasterCaseStatuses.orElse(emptyList()));

        asylumCase.write(FASTER_CASE_STATUSES, allFasterCaseStatuses);

        asylumCase.clear(FASTER_CASE_STATUS);
        asylumCase.clear(FASTER_CASE_STATUS_REASON);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
