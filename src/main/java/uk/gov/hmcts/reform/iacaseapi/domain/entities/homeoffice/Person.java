package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static java.util.Objects.requireNonNull;

public class Person {

    private CodeWithDescription gender;
    private CodeWithDescription nationality;
    private String givenName;
    private String familyName;
    private String fullName;
    private int dayOfBirth;
    private int monthOfBirth;
    private int yearOfBirth;

    private Person() {

    }

    public Person(CodeWithDescription gender, CodeWithDescription nationality,
                  String givenName, String familyName, String fullName,
                  int dayOfBirth, int monthOfBirth, int yearOfBirth) {
        this.gender = gender;
        this.nationality = nationality;
        this.givenName = givenName;
        this.familyName = familyName;
        this.fullName = fullName;
        this.dayOfBirth = dayOfBirth;
        this.monthOfBirth = monthOfBirth;
        this.yearOfBirth = yearOfBirth;
    }

    public CodeWithDescription getGender() {
        return gender;
    }

    public CodeWithDescription getNationality() {
        return nationality;
    }

    public String getGivenName() {
        requireNonNull(givenName);
        return givenName;
    }

    public String getFamilyName() {
        requireNonNull(familyName);
        return familyName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getDayOfBirth() {
        return dayOfBirth;
    }

    public int getMonthOfBirth() {
        return monthOfBirth;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

}
