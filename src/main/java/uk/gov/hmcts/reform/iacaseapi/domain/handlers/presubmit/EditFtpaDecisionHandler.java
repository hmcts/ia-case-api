package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_DECISION_AND_REASONS_DOC_UPLOAD;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_FIRST_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_SECOND_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECOND_FTPA_DECISION_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUMMARISE_FTPA_DECISION_AND_REASONS_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_FTPA_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.valueOf;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CurrentFtpaDecisionResolver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FtpaDisplayService;

@Component
public class EditFtpaDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final Set<String> OUTCOMES_VISIBLE_TO_ALL =
        Set.of("granted", "partiallyGranted", "reheardRule32", "reheardRule35");

    private static final List<String> FTPA_DOCUMENT_GROUPS =
        List.of("GROUNDS_DOCS", "EVIDENCE_DOCS", "OOT_DOCS", "OOT_EXPLANATION", "DOCS");

    private final CurrentFtpaDecisionResolver currentFtpaDecisionResolver;
    private final FtpaDisplayService ftpaDisplayService;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public EditFtpaDecisionHandler(
        CurrentFtpaDecisionResolver currentFtpaDecisionResolver,
        FtpaDisplayService ftpaDisplayService,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.currentFtpaDecisionResolver = currentFtpaDecisionResolver;
        this.ftpaDisplayService = ftpaDisplayService;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPDATE_FTPA_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String ftpaApplicantType = currentFtpaDecisionResolver.getApplicantType(asylumCase)
            .orElseThrow(() -> new IllegalStateException("Can't edit FTPA on this case: No FTPA present"));

        final String party = ftpaApplicantType.toUpperCase();

        final boolean isLegacyDecision = asylumCase
            .read(valueOf("FTPA_APPLICATION_%s_DOCUMENT".formatted(party)), Document.class)
            .isEmpty();

        if (currentFtpaDecisionResolver.isDecisionChanged(asylumCase)) {
            applyNewOutcome(asylumCase, ftpaApplicantType, party);
        }

        if (currentFtpaDecisionResolver.isDocumentUploaded(asylumCase)) {
            addCorrectedDecisionDocument(asylumCase, party, isLegacyDecision);
        }

        refreshFtpaList(asylumCase, ftpaApplicantType, party, isLegacyDecision);

        currentFtpaDecisionResolver.clearUpdateFtpaDecisionFields(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void applyNewOutcome(AsylumCase asylumCase, String ftpaApplicantType, String party) {
        String newOutcome = asylumCase.read(UPDATE_FTPA_DECISION_OUTCOME_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("updateFtpaDecisionOutcomeType is not present"));

        asylumCase.write(
            currentFtpaDecisionResolver.getCurrentOutcomeField(asylumCase, ftpaApplicantType),
            newOutcome
        );

        boolean secondDecisionExists = asylumCase.read(SECOND_FTPA_DECISION_EXISTS, YesOrNo.class)
            .map(exists -> exists == YES)
            .orElse(false);

        asylumCase.write(secondDecisionExists ? FTPA_SECOND_DECISION : FTPA_FIRST_DECISION, newOutcome);

        ftpaDisplayService.setFinalDisplayDecision(
            asylumCase,
            asylumCase.read(FTPA_FIRST_DECISION, String.class).orElse(""),
            asylumCase.read(FTPA_SECOND_DECISION, String.class).orElse("")
        );

        setDecisionVisibility(asylumCase, party, OUTCOMES_VISIBLE_TO_ALL.contains(newOutcome));
    }

    private void setDecisionVisibility(AsylumCase asylumCase, String party, boolean visibleToAll) {
        asylumCase.write(valueOf("IS_%s_FTPA_DECISION_VISIBLE_TO_ALL".formatted(party)), visibleToAll ? YES : NO);

        for (String documentGroup : FTPA_DOCUMENT_GROUPS) {
            asylumCase.write(
                valueOf("IS_FTPA_%s_%s_VISIBLE_IN_DECIDED".formatted(party, documentGroup)),
                visibleToAll ? YES : NO);
            asylumCase.write(
                valueOf("IS_FTPA_%s_%s_VISIBLE_IN_SUBMITTED".formatted(party, documentGroup)),
                visibleToAll ? NO : YES);
        }
    }

    private void addCorrectedDecisionDocument(AsylumCase asylumCase, String party, boolean isLegacyDecision) {
        Document correctedDocument = asylumCase.read(FTPA_DECISION_AND_REASONS_DOC_UPLOAD, Document.class)
            .orElseThrow(() -> new IllegalStateException("ftpaDecisionAndReasonsDocUpload is not present"));

        String summaryOfChanges = asylumCase.read(SUMMARISE_FTPA_DECISION_AND_REASONS_DOCUMENT, String.class)
            .orElseThrow(() -> new IllegalStateException("summariseFtpaDecisionAndReasonsDocument is not present"));

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaDecisionDocuments =
            asylumCase.read(valueOf("ALL_FTPA_%s_DECISION_DOCS".formatted(party)));
        final List<IdValue<DocumentWithMetadata>> existingFtpaDecisionDocuments =
            maybeFtpaDecisionDocuments.orElse(emptyList());

        DocumentWithMetadata correctedDecisionDocument = documentReceiver.receive(
            correctedDocument,
            summaryOfChanges,
            DocumentTag.FTPA_DECISION_AND_REASONS
        );

        asylumCase.write(
            valueOf("ALL_FTPA_%s_DECISION_DOCS".formatted(party)),
            documentsAppender.append(existingFtpaDecisionDocuments, singletonList(correctedDecisionDocument))
        );

        if (!isLegacyDecision) {
            asylumCase.write(valueOf("FTPA_APPLICATION_%s_DOCUMENT".formatted(party)), correctedDocument);
        }
    }

    private void refreshFtpaList(AsylumCase asylumCase, String ftpaApplicantType, String party,
                                 boolean isLegacyDecision) {
        final Optional<List<IdValue<FtpaApplications>>> maybeFtpaApplications = asylumCase.read(FTPA_LIST);
        List<IdValue<FtpaApplications>> ftpaApplications = maybeFtpaApplications.orElse(emptyList());

        ftpaApplications.stream()
            .map(IdValue::getValue)
            .filter(ftpaApplication -> ftpaApplicantType.equals(ftpaApplication.getFtpaApplicant()))
            .findFirst()
            .ifPresent(ftpaApplication -> {
                ftpaDisplayService.mapFtpaDecision(isLegacyDecision, asylumCase, party, ftpaApplication);
                asylumCase.write(FTPA_LIST, ftpaApplications);
            });
    }
}
