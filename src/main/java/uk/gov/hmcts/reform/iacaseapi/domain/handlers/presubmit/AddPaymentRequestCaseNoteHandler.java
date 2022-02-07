package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_PAYMENT_REQUEST_SENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class AddPaymentRequestCaseNoteHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;
    private final FeatureToggler featureToggler;

    private static final String PAYMENT_REQUEST_SENT_ON = "Payment request sent on ";

    public AddPaymentRequestCaseNoteHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        UserDetails userDetails,
        FeatureToggler featureToggler
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT)
               && callback.getEvent().equals(MARK_PAYMENT_REQUEST_SENT)
               && featureToggler.getValue("wa-R2-feature", false
        );
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

        String paymentRequestSentDate = asylumCase
            .read(PAYMENT_REQUEST_SENT_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("paymentRequestSentDate is not present"));

        String paymentRequestSentNoteDescription = asylumCase
            .read(PAYMENT_REQUEST_SENT_NOTE_DESCRIPTION, String.class)
            .orElseThrow(() -> new IllegalStateException("paymentRequestSentNoteDescription is not present"));

        Document paymentRequestSentDocument = asylumCase
            .read(PAYMENT_REQUEST_SENT_DOCUMENT, Document.class)
            .orElseThrow(() -> new IllegalStateException("paymentRequestSentDocument is not present"));

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            asylumCase.read(CASE_NOTES);

        final CaseNote newCaseNote = new CaseNote(
            PAYMENT_REQUEST_SENT_ON + paymentRequestSentDate,
            paymentRequestSentNoteDescription,
            buildFullName(),
            dateProvider.now().toString()
        );

        newCaseNote.setCaseNoteDocument(paymentRequestSentDocument);

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        asylumCase.write(CASE_NOTES, allCaseNotes);

        asylumCase.clear(PAYMENT_REQUEST_SENT_DATE);
        asylumCase.clear(PAYMENT_REQUEST_SENT_NOTE_DESCRIPTION);
        asylumCase.clear(PAYMENT_REQUEST_SENT_DOCUMENT);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
               + " "
               + userDetails.getSurname();
    }
}
