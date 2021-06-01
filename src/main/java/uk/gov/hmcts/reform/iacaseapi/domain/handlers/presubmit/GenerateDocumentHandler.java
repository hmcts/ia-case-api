package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@Component
public class GenerateDocumentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isDocmosisEnabled;
    private final boolean isEmStitchingEnabled;
    private final DocumentGenerator<AsylumCase> documentGenerator;
    private final DateProvider dateProvider;
    private final boolean isSaveAndContinueEnabled;

    public GenerateDocumentHandler(
            @Value("${featureFlag.docmosisEnabled}") boolean isDocmosisEnabled,
            @Value("${featureFlag.isEmStitchingEnabled}") boolean isEmStitchingEnabled,
            DocumentGenerator<AsylumCase> documentGenerator,
            DateProvider dateProvider,
            @Value("${featureFlag.isSaveAndContinueEnabled}") boolean isSaveAndContinueEnabled) {
        this.isDocmosisEnabled = isDocmosisEnabled;
        this.isEmStitchingEnabled = isEmStitchingEnabled;
        this.documentGenerator = documentGenerator;
        this.dateProvider = dateProvider;
        this.isSaveAndContinueEnabled = isSaveAndContinueEnabled;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> allowedEvents = Lists.newArrayList(
            Event.SUBMIT_APPEAL,
            Event.PAY_AND_SUBMIT_APPEAL,
            Event.DRAFT_HEARING_REQUIREMENTS,
            Event.UPDATE_HEARING_REQUIREMENTS,
            Event.LIST_CASE,
            Event.GENERATE_HEARING_BUNDLE,
            Event.CUSTOMISE_HEARING_BUNDLE,
            Event.GENERATE_DECISION_AND_REASONS,
            Event.SEND_DECISION_AND_REASONS,
            Event.EDIT_CASE_LISTING,
            Event.ADJOURN_HEARING_WITHOUT_DATE,
            Event.END_APPEAL,
            Event.SUBMIT_CMA_REQUIREMENTS,
            Event.LIST_CMA,
            Event.END_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.GENERATE_UPPER_TRIBUNAL_BUNDLE);
        if (isEmStitchingEnabled) {
            allowedEvents.add(Event.SUBMIT_CASE);
            if (!isSaveAndContinueEnabled) {
                allowedEvents.add(Event.BUILD_CASE);
            }
        }

        return isDocmosisEnabled
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && allowedEvents.contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithGeneratedDocument = documentGenerator.generate(callback);

        if (Event.EDIT_CASE_LISTING.equals(callback.getEvent())) {
            removeFlagsForRecordedApplication(
                asylumCaseWithGeneratedDocument,
                callback.getCaseDetails().getState()
            );
            changeEditListingApplicationsToCompleted(asylumCaseWithGeneratedDocument);
        }

        if (Event.SEND_DECISION_AND_REASONS.equals(callback.getEvent())) {
            saveDecisionDetails(asylumCaseWithGeneratedDocument);
        }

        return new PreSubmitCallbackResponse<>(asylumCaseWithGeneratedDocument);
    }

    private void saveDecisionDetails(AsylumCase asylumCase) {
        asylumCase.write(
            APPEAL_DECISION,
            StringUtils.capitalize(
                asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)
                    .orElseThrow(() -> new IllegalStateException("decision property must be set"))
                    .getValue()
            )
        );
        asylumCase.write(APPEAL_DATE, dateProvider.now().toString());
        asylumCase.write(APPEAL_DECISION_AVAILABLE, YesOrNo.YES);
    }

    private void changeEditListingApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.ADJOURN.toString().equals(applicationType)
                    || ApplicationType.EXPEDITE.toString().equals(applicationType)
                    || ApplicationType.TRANSFER.toString().equals(applicationType)) {

                    return new IdValue<>(application.getId(), new Application(
                        application.getValue().getApplicationDocuments(),
                        application.getValue().getApplicationSupplier(),
                        applicationType,
                        application.getValue().getApplicationReason(),
                        application.getValue().getApplicationDate(),
                        application.getValue().getApplicationDecision(),
                        application.getValue().getApplicationDecisionReason(),
                        application.getValue().getApplicationDateOfDecision(),
                        "Completed"
                    ));
                }

                return application;
            })
            .collect(Collectors.toList())
        );
    }

    private void removeFlagsForRecordedApplication(AsylumCase asylumCase, State currentState) {

        boolean isApplicationRecorded = asylumCase.read(APPLICATION_EDIT_LISTING_EXISTS, String.class)
            .map(flag -> flag.equalsIgnoreCase("Yes"))
            .orElse(false);

        if (isApplicationRecorded) {
            asylumCase.clear(APPLICATION_EDIT_LISTING_EXISTS);

            boolean isWithdrawExists = asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)
                .map(flag -> flag.equalsIgnoreCase("Yes"))
                .orElse(false);

            if (!isWithdrawExists) {
                asylumCase.clear(DISABLE_OVERVIEW_PAGE);
                asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, currentState);
            }
        }
    }
}
