package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import java.util.Comparator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;

class AppealReferenceNumberComparator implements Comparator<AppealReferenceNumber> {

    @Override
    public int compare(AppealReferenceNumber o1, AppealReferenceNumber o2) {

        if (Integer.valueOf(o1.getYear()) > Integer.valueOf(o2.getYear())) {
            return -1;
        }

        if (Integer.valueOf(o1.getYear()) < Integer.valueOf(o2.getYear())) {
            return 1;
        }

        if (Integer.valueOf(o1.getSequence()) > Integer.valueOf(o2.getSequence())) {
            return -1;
        }

        if (Integer.valueOf(o1.getSequence()) < Integer.valueOf(o2.getSequence())) {
            return 1;
        }

        return 0;
    }
}
