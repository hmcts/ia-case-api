package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class WitnessNamesUpdateServiceTest {

    @Mock
    private AsylumCase asylumCase;

    private final String witnessFirstName = "FirstName";
    private final String witnessFamilyName = "FamilyName";
    private WitnessDetails witnessDetails1;
    private WitnessDetails witnessDetails2;
    private WitnessDetails witnessDetails3;
    private WitnessDetails witnessDetails5;
    private WitnessDetails witnessDetails6;
    private WitnessDetails witnessDetails7;
    private WitnessDetails witnessDetails8;
    private WitnessDetails witnessDetails9;
    private List<IdValue<WitnessDetails>> witnessDetailsIdValueList;

    @BeforeEach
    void setUp() {

        witnessDetails1 = new WitnessDetails();
        witnessDetails1.setWitnessName(witnessFirstName + " " + witnessFamilyName);

        witnessDetails2 = new WitnessDetails();
        witnessDetails2.setWitnessName(witnessFirstName);

        witnessDetails3 = new WitnessDetails();
        witnessDetails3.setWitnessName(witnessFirstName);
        witnessDetails3.setWitnessFamilyName(witnessFamilyName);

        witnessDetails5 = new WitnessDetails();
        witnessDetails5.setWitnessName(witnessFirstName + " " + witnessFamilyName);
        witnessDetails5.setWitnessFamilyName("");

        witnessDetails6 = new WitnessDetails();
        witnessDetails6.setWitnessName(witnessFirstName + " " + witnessFamilyName);
        witnessDetails6.setWitnessFamilyName(" ");

        witnessDetails7 = new WitnessDetails();

        witnessDetails8 = new WitnessDetails();
        witnessDetails8.setWitnessName("");

        witnessDetails9 = new WitnessDetails();
        witnessDetails9.setWitnessName(" ");

        witnessDetailsIdValueList = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2),
            new IdValue<>("3", witnessDetails3),
            new IdValue<>("5", witnessDetails5),
            new IdValue<>("6", witnessDetails6),
            new IdValue<>("7", witnessDetails7),
            new IdValue<>("8", witnessDetails8),
            new IdValue<>("9", witnessDetails9)
        );
    }

    @Test
    void testFix() {
        when(asylumCase.read(AsylumCaseFieldDefinition.WITNESS_DETAILS))
            .thenReturn(Optional.of(witnessDetailsIdValueList));

        WitnessNamesUpdateService.update(asylumCase);

        witnessDetailsIdValueList.forEach(idValue -> {
            switch (idValue.getId()) {
                case "1":
                    assertEquals(witnessFirstName, witnessDetails1.getWitnessName());
                    assertEquals(witnessFamilyName, witnessDetails1.getWitnessFamilyName());
                    break;
                case "2":
                    assertEquals(witnessFirstName, witnessDetails2.getWitnessName());
                    assertEquals(" ", witnessDetails2.getWitnessFamilyName());
                    break;
                case "3":
                    assertEquals(witnessFirstName, witnessDetails3.getWitnessName());
                    assertEquals(witnessFamilyName, witnessDetails3.getWitnessFamilyName());
                    break;
                case "5":
                    assertEquals(witnessFirstName, witnessDetails5.getWitnessName());
                    assertEquals(witnessFamilyName, witnessDetails5.getWitnessFamilyName());
                    break;
                case "6":
                    assertEquals(witnessFirstName, witnessDetails6.getWitnessName());
                    assertEquals(witnessFamilyName, witnessDetails6.getWitnessFamilyName());
                    break;
                case "7":
                    assertNull(witnessDetails7.getWitnessName());
                    assertNull(witnessDetails7.getWitnessFamilyName());
                    break;
                case "8":
                    assertTrue(witnessDetails8.getWitnessName().isEmpty());
                    assertNull(witnessDetails8.getWitnessFamilyName());
                    break;
                case "9":
                    assertEquals(" ", witnessDetails9.getWitnessName());
                    assertEquals(" ", witnessDetails9.getWitnessFamilyName());
                    break;
                default:
                    break;
            }
        });
    }

}
