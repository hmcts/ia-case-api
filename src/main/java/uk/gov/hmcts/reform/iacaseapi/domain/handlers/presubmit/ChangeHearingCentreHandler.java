package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.NEWPORT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@Component
public class ChangeHearingCentreHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CaseManagementLocationService caseManagementLocationService;

    public ChangeHearingCentreHandler(CaseManagementLocationService caseManagementLocationService) {
        this.caseManagementLocationService = caseManagementLocationService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.CHANGE_HEARING_CENTRE;
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

        HearingCentre maybeHearingCentre;

        if (isCaseUsingLocationRefData(asylumCase)) {
            Value refDataHearingCentre =
                asylumCase.read(HEARING_CENTRE_DYNAMIC_LIST, DynamicList.class)
                    .map(h -> h.getValue())
                    .orElseThrow(() -> new IllegalStateException("hearingCentreDynamicList is not present"));


            HandlerUtils.setSelectedHearingCentreRefDataField(asylumCase, refDataHearingCentre.getLabel());
            maybeHearingCentre = HearingCentre.fromEpimsId(refDataHearingCentre.getCode(), false)
                .orElse(NEWPORT);

            asylumCase.write(CASE_MANAGEMENT_LOCATION_REF_DATA,
                caseManagementLocationService.getRefDataCaseManagementLocation(
                    StaffLocation.getLocation(maybeHearingCentre).getName()));
        } else {
            maybeHearingCentre =
                asylumCase.read(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, HearingCentre.class)
                    .orElse(NEWPORT);
        }

        asylumCase.write(HEARING_CENTRE, maybeHearingCentre);
        asylumCase.write(IS_VIRTUAL_HEARING,
                Objects.equals(maybeHearingCentre.getEpimsId(), HearingCentre.IAC_NATIONAL_VIRTUAL.getEpimsId())
                        ? YesOrNo.YES : YesOrNo.NO);

        State maybePreviousState =
            asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class).orElse(State.UNKNOWN);

        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, maybePreviousState);

        String staffLocationName = StaffLocation.getLocation(maybeHearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION,
            caseManagementLocationService.getCaseManagementLocation(staffLocationName));

        changeHearingCentreApplicationsToCompleted(asylumCase);

        asylumCase.clear(APPLICATION_CHANGE_HEARING_CENTRE_EXISTS);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void changeHearingCentreApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.CHANGE_HEARING_CENTRE.toString().equals(applicationType)) {

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
}

