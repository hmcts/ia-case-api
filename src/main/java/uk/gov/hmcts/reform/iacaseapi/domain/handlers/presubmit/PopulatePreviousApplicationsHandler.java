package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_APPLICATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PRIOR_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.VIEW_PREVIOUS_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeNewApplicationService;

@Component
public class PopulatePreviousApplicationsHandler implements PreSubmitCallbackHandler<BailCase> {

    private final MakeNewApplicationService makeNewApplicationService;

    public PopulatePreviousApplicationsHandler(MakeNewApplicationService makeNewApplicationService) {
        this.makeNewApplicationService = makeNewApplicationService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_START && callback.getEvent() == VIEW_PREVIOUS_APPLICATIONS;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();
        Optional<List<IdValue<PriorApplication>>> maybePriorApplications = bailCase.read(PRIOR_APPLICATIONS);

        List<IdValue<PriorApplication>> priorApplications = maybePriorApplications.orElse(emptyList());

        if (priorApplications.isEmpty()) {
            PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);
            response.addError("There is no previous application to view");
            return response;
        }

        List<Value> previousApplicationsElements = priorApplications
            .stream()
            .map(idValue -> {
                String priorCaseJson = idValue.getValue().getCaseDataJson();
                BailCase priorCase = makeNewApplicationService
                    .getBailCaseFromString(idValue.getValue().getCaseDataJson());

                String decisionStr;
                String decisionDateStr;
                if (priorCaseJson.contains("recordDecisionType")) {
                    decisionStr = "Decided ";
                    decisionDateStr = convertDate(priorCase.read(DECISION_DETAILS_DATE, String.class).orElse(""));
                } else if (priorCaseJson.contains("endApplicationOutcome")) {
                    decisionStr = "Ended ";
                    decisionDateStr = convertDate(priorCase.read(END_APPLICATION_DATE, String.class).orElse(""));
                } else {
                    throw new RequiredFieldMissingException("Missing Decision Details");
                }

                return new Value(idValue.getValue().getApplicationId(),
                                 "Bail Application "
                                     + idValue.getValue().getApplicationId()
                                     + " - " + decisionStr + decisionDateStr);
            })
            .collect(Collectors.toList());
        Collections.reverse(previousApplicationsElements);
        DynamicList previousApplications = new DynamicList(previousApplicationsElements.get(0),
                                                           previousApplicationsElements);

        bailCase.write(PREVIOUS_APPLICATION_LIST, previousApplications);
        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private String convertDate(String date) {
        if (date.isEmpty()) {
            throw new RequiredFieldMissingException("Missing Decision Date");
        }
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
