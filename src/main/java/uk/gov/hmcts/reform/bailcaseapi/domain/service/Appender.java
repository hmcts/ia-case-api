package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class Appender<T> {

    public List<IdValue<T>> append(
        T newItem,
        List<IdValue<T>> existingItems
    ) {

        requireNonNull(newItem);

        final List<IdValue<T>> allItems = new ArrayList<>();

        int index = existingItems.size() + 1;

        IdValue<T> itemIdValue = new IdValue<>(String.valueOf(index--), newItem);

        allItems.add(itemIdValue);

        for (IdValue<T> existingItem : existingItems) {
            allItems.add(new IdValue<>(
                String.valueOf(index--),
                existingItem.getValue()));
        }

        return allItems;
    }
}
