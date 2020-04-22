package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class AddHearingHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<Hearing> hearingAppender;
    private final DateProvider dateProvider;

    public AddHearingHandler(
        Appender<Hearing> hearingAppender,
        DateProvider dateProvider
    ) {
        this.hearingAppender = hearingAppender;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(ADD_HEARING);
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

        // attendance
        String attendingJudge = asylumCase
            .read(ATTENDING_JUDGE, String.class)
            .orElse("");
        String attendingAppellant = asylumCase
            .read(ATTENDING_APPELLANT, String.class)
            .orElse("");
        String attendingAppellantsLegalRepresentative = asylumCase
            .read(ATTENDING_APPELLANT_LEGAL_REPRESENTATIVE, String.class)
            .orElse("");
        String attendingHomeOfficeLegalRepresentative = asylumCase
            .read(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE, String.class)
            .orElse("");
        HoursAndMinutes actualCaseHearingLength = asylumCase
            .read(ACTUAL_CASE_HEARING_LENGTH, HoursAndMinutes.class)
            .orElse(new HoursAndMinutes("",""));

        // hearing details
        String ariaListingReference = asylumCase
            .read(ARIA_LISTING_REFERENCE, String.class)
            .orElse("");
        HearingCentre listCaseHearingCentre = asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElse(null);
        HearingLength listCaseHearingLength = asylumCase
            .read(LIST_CASE_HEARING_LENGTH, HearingLength.class)
            .orElse(null);
        String listCaseHearingDate = asylumCase
            .read(LIST_CASE_HEARING_DATE, String.class)
            .orElse("");

        List<IdValue<HearingRecordingDocument>> recordingDocuments = asylumCase
            .<List<IdValue<HearingRecordingDocument>>>read(HEARING_RECORDING_DOCUMENTS)
            .orElse(Collections.emptyList());

        HearingType hearingType = asylumCase
            .read(HEARING_TYPE, HearingType.class)
            .orElse(HearingType.HEARING);

        // record hearing
        List<IdValue<Hearing>> existingPreviousHearings = asylumCase
            .<List<IdValue<Hearing>>>read(PREVIOUS_HEARINGS)
            .orElse(emptyList());

        final Hearing newHearing = new Hearing(
            attendingJudge,
            attendingAppellant,
            attendingAppellantsLegalRepresentative,
            attendingHomeOfficeLegalRepresentative,
            actualCaseHearingLength,
            ariaListingReference,
            listCaseHearingCentre,
            listCaseHearingLength.getValue() + " minutes",
            listCaseHearingDate,
            recordingDocuments,
            hearingType,
            dateProvider.now().toString()
        );

        List<IdValue<Hearing>> allPreviousHearings =
            hearingAppender.append(newHearing, existingPreviousHearings);

        asylumCase.write(PREVIOUS_HEARINGS, allPreviousHearings);


        // override old values

        // attendance
        String addAttendingJudge = asylumCase
            .read(ADD_ATTENDING_JUDGE, String.class)
            .orElse("");
        String addAttendingAppellant = asylumCase
            .read(ADD_ATTENDING_APPELLANT, String.class)
            .orElse("");
        String addAttendingAppellantsLegalRepresentative = asylumCase
            .read(ADD_ATTENDING_APPELLANT_LEGAL_REPRESENTATIVE, String.class)
            .orElse("");
        String addAttendingHomeOfficeLegalRepresentative = asylumCase
            .read(ADD_ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE, String.class)
            .orElse("");
        HoursAndMinutes addActualCaseHearingLength = asylumCase
            .read(ADD_ACTUAL_CASE_HEARING_LENGTH, HoursAndMinutes.class)
            .orElse(new HoursAndMinutes("",""));

        // hearing details
        String addAriaListingReference = asylumCase
            .read(ADD_ARIA_LISTING_REFERENCE, String.class)
            .orElse("");
        HearingCentre addListCaseHearingCentre = asylumCase
            .read(ADD_LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElse(null);
        HearingLength addListCaseHearingLength = asylumCase
            .read(ADD_LIST_CASE_HEARING_LENGTH, HearingLength.class)
            .orElse(null);
        String addListCaseHearingDate = asylumCase
            .read(ADD_LIST_CASE_HEARING_DATE, String.class)
            .orElse("");

        List<IdValue<HearingRecordingDocument>> addRecordingDocuments = asylumCase
            .<List<IdValue<HearingRecordingDocument>>>read(ADD_HEARING_RECORDING_DOCUMENTS)
            .orElse(Collections.emptyList());

        asylumCase.write(ATTENDING_JUDGE, addAttendingJudge);
        asylumCase.write(ATTENDING_APPELLANT, addAttendingAppellant);
        asylumCase.write(ATTENDING_APPELLANT_LEGAL_REPRESENTATIVE, addAttendingAppellantsLegalRepresentative);
        asylumCase.write(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE, addAttendingHomeOfficeLegalRepresentative);
        asylumCase.write(ACTUAL_CASE_HEARING_LENGTH, addActualCaseHearingLength);
        asylumCase.write(ARIA_LISTING_REFERENCE, addAriaListingReference);
        asylumCase.write(LIST_CASE_HEARING_CENTRE, addListCaseHearingCentre);
        // TODO does not work in current form - need to investigate why - showing null validation error in frontend
        //asylumCase.write(LIST_CASE_HEARING_LENGTH, addListCaseHearingLength);
        asylumCase.write(LIST_CASE_HEARING_DATE, addListCaseHearingDate);
        asylumCase.write(ADD_HEARING_RECORDING_DOCUMENTS, addRecordingDocuments);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
