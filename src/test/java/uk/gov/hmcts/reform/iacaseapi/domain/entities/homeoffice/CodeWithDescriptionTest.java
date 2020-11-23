package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CodeWithDescriptionTest {

    private CodeWithDescription codeWithDescription;

    @Test
    public void has_correct_values() {

        codeWithDescription = new CodeWithDescription("HMCTS", "HM Courts and Tribunal Service");
        assertEquals("HMCTS", codeWithDescription.getCode());
        assertEquals("HM Courts and Tribunal Service", codeWithDescription.getDescription());
    }


}
