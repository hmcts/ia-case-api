package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.valueOf;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.*;

@Component
public class FtpaMigrateLegacyDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final Appender<FtpaApplications> ftpaAppender;
    private final FtpaDisplayService ftpaDisplayService;

    private final List<String> parties = List.of(APPELLANT.toString(), RESPONDENT.toString());

    public FtpaMigrateLegacyDataHandler(FeatureToggler featureToggler,
                                        Appender<FtpaApplications> ftpaAppender,
                                        FtpaDisplayService ftpaDisplayService) {
        this.featureToggler = featureToggler;
        this.ftpaAppender = ftpaAppender;
        this.ftpaDisplayService = ftpaDisplayService;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && featureToggler.getValue("dlrm-setaside-feature-flag", false)
                && Arrays.asList(
                Event.APPLY_FOR_FTPA_APPELLANT,
                Event.APPLY_FOR_FTPA_RESPONDENT,
                Event.DECIDE_FTPA_APPLICATION
        ).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final AsylumCase asylumCaseBefore =
                callback.getCaseDetailsBefore()
                        .orElseThrow(() -> new IllegalStateException("cannot find previous details"))
                        .getCaseData();

        Optional<List<IdValue<FtpaApplications>>> maybeExistingFtpaApplictions = asylumCase.read(FTPA_LIST);
        List<IdValue<FtpaApplications>> existingApplications = maybeExistingFtpaApplictions.orElse(emptyList());

        if (existingApplications.isEmpty()) {
            for (String party : parties) {
                boolean isFtpaDecidedBefore = asylumCaseBefore
                        .read(valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", party.toUpperCase()))).isPresent();
                if (isFtpaDecidedBefore) {
                    existingApplications = migrateFtpaDecided(asylumCaseBefore, existingApplications, party);
                }

                boolean isFtpaStartedBefore = asylumCaseBefore
                        .read(valueOf(String.format("FTPA_%s_GROUNDS_DOCUMENTS", party.toUpperCase()))).isPresent();
                if (isFtpaStartedBefore) {
                    existingApplications = migrateFtpaStarted(asylumCaseBefore, existingApplications, party);
                }
            }

            asylumCase.write(FTPA_LIST, existingApplications);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
        
    }

    private List<IdValue<FtpaApplications>> migrateFtpaStarted(AsylumCase asylumCaseBefore,
                                                               List<IdValue<FtpaApplications>> existingApplications,
                                                               String ftpaApplicantType) {
        final String ftpaApplicantUpperCase = ftpaApplicantType.toUpperCase();

        String applicationDate = asylumCaseBefore
                .read(valueOf(String.format("FTPA_%s_APPLICATION_DATE", ftpaApplicantUpperCase)), String.class)
                .orElseThrow(() -> new IllegalStateException("ftpaApplicationDate is not present"));

        Optional<IdValue<FtpaApplications>> existingFtpaApplication = existingApplications.stream()
                .filter(ftpaApp -> ftpaApp.getValue().getFtpaApplicant().equals(ftpaApplicantType)
                        && !LocalDate.parse(ftpaApp.getValue().getFtpaDecisionDate()).isBefore(LocalDate.parse(applicationDate)))
                .findFirst();

        final FtpaApplications newFtpaApplication = existingFtpaApplication.isPresent()
                ? existingFtpaApplication.get().getValue()
                : FtpaApplications.builder().ftpaApplicant(ftpaApplicantType).build();

        final Optional<List<IdValue<DocumentWithDescription>>> maybeGroundsDocument =
                asylumCaseBefore.read(valueOf(String.format("FTPA_%s_GROUNDS_DOCUMENTS", ftpaApplicantUpperCase)));
        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaEvidence =
                asylumCaseBefore.read(valueOf(String.format("FTPA_%s_EVIDENCE_DOCUMENTS", ftpaApplicantUpperCase)));
        final Optional<List<IdValue<DocumentWithDescription>>> maybeOutOfTimeDocuments =
                asylumCaseBefore.read(valueOf(String.format("FTPA_%s_OUT_OF_TIME_DOCUMENTS", ftpaApplicantUpperCase)));
        final String ftpaOutOfTimeExplanation =
                asylumCaseBefore.read(valueOf(String.format("FTPA_%s_OUT_OF_TIME_EXPLANATION", ftpaApplicantUpperCase)), String.class)
                        .orElse("");

        newFtpaApplication.setFtpaApplicationDate(applicationDate);
        maybeGroundsDocument.ifPresent(newFtpaApplication::setFtpaGroundsDocuments);
        maybeFtpaEvidence.ifPresent(newFtpaApplication::setFtpaEvidenceDocuments);
        maybeOutOfTimeDocuments.ifPresent(newFtpaApplication::setFtpaOutOfTimeDocuments);
        if (!ftpaOutOfTimeExplanation.isEmpty()) {
            newFtpaApplication.setFtpaOutOfTimeExplanation(ftpaOutOfTimeExplanation);
        }

        return existingFtpaApplication.isPresent()
                ? existingApplications : ftpaAppender.append(newFtpaApplication, existingApplications);
    }

    private List<IdValue<FtpaApplications>> migrateFtpaDecided(AsylumCase asylumCaseBefore,
                                                               List<IdValue<FtpaApplications>> existingApplications,
                                                               String ftpaApplicantType) {
        final String ftpaApplicantUpperCase = ftpaApplicantType.toUpperCase();

        final FtpaApplications newFtpaApplication =
                FtpaApplications.builder()
                        .ftpaApplicant(ftpaApplicantType)
                        .build();

        ftpaDisplayService.mapFtpaDecision(true, asylumCaseBefore, ftpaApplicantUpperCase, newFtpaApplication);

        return ftpaAppender.append(newFtpaApplication, existingApplications);
    }
}
