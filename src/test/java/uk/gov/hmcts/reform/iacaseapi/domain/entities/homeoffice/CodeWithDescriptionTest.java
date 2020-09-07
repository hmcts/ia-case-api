package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
