package uk.gov.hmcts.reform.iacaseapi.events.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

@Service
public class FirstUnapprovedTimeExtensionExtractor {

    public Optional<TimeExtension> extract(
        AsylumCase asylumCase
    ) {
        if (asylumCase
            .getTimeExtensions()
            .isPresent()) {

            if (asylumCase
                .getTimeExtensions()
                .get()
                .getTimeExtensions()
                .isPresent()) {

                return
                    asylumCase
                        .getTimeExtensions()
                        .get()
                        .getTimeExtensions()
                        .get()
                        .stream()
                        .map(IdValue::getValue)
                        .filter(timeExtension ->
                            timeExtension
                                .getStatus()
                                .orElse("")
                                .equals("awaitingApproval")
                        )
                        .findFirst();
            }
        }

        return Optional.empty();
    }
}
