package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class DecideAnApplicationMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.DECIDE_AN_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        DynamicList maybeMakeAnApplicationsList = asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Make an applications list not present"));

        List<Value> allMakeAnApplicationsListElements = getAllMakeAnApplicationsListElements(asylumCase);

        String code = maybeMakeAnApplicationsList.getValue().getCode();
        allMakeAnApplicationsListElements
            .stream()
            .filter(application -> application.getCode().equals(code))
            .findFirst()
            .ifPresent(idValue -> {
                List<Value> applicationsPending = getMakeAnApplicationsListElementsPending(asylumCase);
                DynamicList dynamicListPending = new DynamicList(maybeMakeAnApplicationsList.getValue(), applicationsPending);
                asylumCase.write(MAKE_AN_APPLICATIONS_LIST, dynamicListPending);
            });

        setSelectedApplicationFields(asylumCase, code);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<Value> getAllMakeAnApplicationsListElements(AsylumCase asylumCase) {

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);
        AtomicInteger counter = new AtomicInteger(1);

        return mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(
                idValue.getId(),
                idValue.getValue().getApplicant() + " : Application " + counter.getAndIncrement()))
            .collect(Collectors.toList());
    }

    private List<Value> getMakeAnApplicationsListElementsPending(AsylumCase asylumCase) {

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);
        AtomicInteger counter = new AtomicInteger(1);

        return mayBeMakeAnApplications
                .orElse(Collections.emptyList())
                .stream()
                .map(idValue -> new Value(
                        idValue.getId(),
                        idValue.getValue().getApplicant() + idValue.getValue().getDecision() + " : Application " + counter.getAndIncrement()))
                .filter(val -> val.getLabel().contains("Pending"))
                .map(val -> new Value(
                        val.getCode(),
                            val.getLabel().replace("Pending","")))
                .collect(Collectors.toList());

    }

    private void setSelectedApplicationFields(AsylumCase asylumCase, String code) {

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

        mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .filter(application -> application.getId().equals(code))
            .findFirst()
            .ifPresent(idValue -> {
                MakeAnApplication makeAnApplication = idValue.getValue();

                StringBuilder documentsLink = new StringBuilder();
                makeAnApplication.getEvidence()
                    .stream()
                    .forEach(evidence -> {
                        String docName = evidence.getValue().getDocumentFilename();
                        String docUrl = evidence.getValue().getDocumentBinaryUrl();
                        docUrl = docUrl.substring(docUrl.indexOf("/documents"));
                        documentsLink.append("</br><a href='" + docUrl + "' target='_blank'>" + docName + "</a>");
                    });

                ImmutableMap.Builder<String, String> makeAnApplicationFields = ImmutableMap.<String, String>builder()
                    .put("Type of application", makeAnApplication.getType())
                    .put("Application details", makeAnApplication.getDetails())
                    .put("Documents supporting application", documentsLink.toString())
                    .put("Date application was made", LocalDate.parse(makeAnApplication.getDate()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));


                asylumCase.write(MAKE_AN_APPLICATION_FIELDS, getApplicationFieldsHtmlView(makeAnApplicationFields));
            });
    }

    private String getApplicationFieldsHtmlView(ImmutableMap.Builder<String, String> makeAnApplicationFields) {

        StringBuilder sb = new StringBuilder();
        sb.append("<table>");

        makeAnApplicationFields
            .build()
            .entrySet()
            .stream()
            .forEach(field -> {
                sb.append("<tr><td>" + field.getKey() + "</td>");
                sb.append("<td>" + field.getValue() + "</td></tr>");
            });
        sb.append("</table>");

        return sb.toString();
    }
}
