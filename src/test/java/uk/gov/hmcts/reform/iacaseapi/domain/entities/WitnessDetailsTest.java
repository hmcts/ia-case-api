package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WitnessDetailsTest {

    private final String witnessFirstName = "FirstName";
    private final String witnessFamilyName = "FamilyName";
    private WitnessDetails witnessDetails1;
    private WitnessDetails witnessDetails2;
    private WitnessDetails witnessDetails3;

    @BeforeEach
    void setUp() {
        witnessDetails1 = new WitnessDetails();
        witnessDetails1.setWitnessName(witnessFirstName + " " + witnessFamilyName);

        witnessDetails2 = new WitnessDetails();

        witnessDetails3 = new WitnessDetails();
        witnessDetails3.setWitnessName(" ");
    }

    @Test
    void testBuildWitnessFullName() {
        String fullName = witnessFirstName + " " + witnessFamilyName;

        assertEquals(fullName, witnessDetails1.buildWitnessFullName());
        assertEquals(" ", witnessDetails2.buildWitnessFullName());
        assertEquals(witnessDetails3.getWitnessName(), witnessDetails3.buildWitnessFullName());
    }
}





