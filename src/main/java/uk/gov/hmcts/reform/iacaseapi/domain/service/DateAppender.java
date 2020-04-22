package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousDates;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

public class DateAppender {

    private DateAppender() {}

    public static List<IdValue<PreviousDates>> appendPreviousDates(List<IdValue<PreviousDates>> previousDates, String dateDue, String dateSent) {
        if (CollectionUtils.isEmpty(previousDates)) {

            return newArrayList(new IdValue<>("1", new PreviousDates(dateDue, dateSent)));
        } else {

            int newEntryId = previousDates.size() + 1;
            List<IdValue<PreviousDates>> newEntry = Collections.singletonList(
                new IdValue<>(String.valueOf(newEntryId),
                    new PreviousDates(dateDue, dateSent)));

            Stream<IdValue<PreviousDates>> combinedStream = Stream.of(newEntry, previousDates)
                .flatMap(Collection::stream);

            return combinedStream.collect(Collectors.toList());
        }
    }
}
