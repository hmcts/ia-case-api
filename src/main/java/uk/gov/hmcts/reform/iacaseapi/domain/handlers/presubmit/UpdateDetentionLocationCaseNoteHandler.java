package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_DETENTION_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class UpdateDetentionLocationCaseNoteHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public UpdateDetentionLocationCaseNoteHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT)
               && callback.getEvent().equals(UPDATE_DETENTION_LOCATION);
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
        CaseDetails<AsylumCase> mayBePreviousCaseDetails = callback.getCaseDetailsBefore().orElseThrow(() -> new IllegalStateException("cannot find previous Case details"));
        AsylumCase caseDataBefore = mayBePreviousCaseDetails.getCaseData();

        String caseNoteDesc = "The detention location for this appellant has changed from: \n\n"
                + getDetentionLocationForCaseNote(caseDataBefore)
                + "\n\n to: \n\n"
                + getDetentionLocationForCaseNote(asylumCase);
        final String caseNoteSubject = "Updated detention location";

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            asylumCase.read(CASE_NOTES);

        final CaseNote newCaseNote = new CaseNote(
            caseNoteSubject,
            caseNoteDesc,
            buildFullName(),
            dateProvider.now().toString()
        );

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        asylumCase.write(CASE_NOTES, allCaseNotes);
        asylumCase.write(PREVIOUS_DETENTION_LOCATION, getDetentionLocation(caseDataBefore));

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
               + " "
               + userDetails.getSurname();
    }

    private String getDetentionLocationForCaseNote(AsylumCase asylumCase) {
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElse("");
        switch (detentionFacility) {
            case "immigrationRemovalCentre":
                return getFacilityName(IRC_NAME, asylumCase) + " IRC";
            case "prison":
                return getFacilityName(PRISON_NAME, asylumCase) + " Prison";
            case "other":
                return asylumCase.read(OTHER_DETENTION_FACILITY_NAME, OtherDetentionFacilityName.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Other detention facility name is missing")).getOther();
            default:
                throw new RequiredFieldMissingException("Detention Facility is missing");
        }
    }

    private String getDetentionLocation(AsylumCase asylumCase) {
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElse("");
        switch (detentionFacility) {
            case "immigrationRemovalCentre":
                return getFacilityName(IRC_NAME, asylumCase);
            case "prison":
                return getFacilityName(PRISON_NAME, asylumCase);
            case "other":
                return asylumCase.read(OTHER_DETENTION_FACILITY_NAME, OtherDetentionFacilityName.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Other detention facility name is missing")).getOther();
            default:
                throw new RequiredFieldMissingException("Detention Facility is missing");
        }
    }

    private String getFacilityName(AsylumCaseFieldDefinition field, AsylumCase asylumCase) {
        return asylumCase.read(field, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException(field.name() + " is missing"));
    }
}
