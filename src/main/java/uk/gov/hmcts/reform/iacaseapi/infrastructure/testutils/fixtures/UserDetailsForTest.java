package uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures;

import java.util.Set;
import lombok.Data;

@Data
public class UserDetailsForTest {

    private String id;
    private Set<String> roles;
    private String email;
    private String forename;
    private String surname;

    UserDetailsForTest(String id, Set<String> roles, String email, String forename, String surname) {
        this.id = id;
        this.roles = roles;
        this.email = email;
        this.forename = forename;
        this.surname = surname;
    }

    public static class UserDetailsForTestBuilder implements Builder<UserDetailsForTest> {

        public static UserDetailsForTestBuilder userWith() {
            return new UserDetailsForTestBuilder();
        }

        private String id = "1";
        private Set<String> roles;
        private String email = "someone@somewhere.com";
        private String forename = "some-forename";
        private String surname = "some-surname";

        public UserDetailsForTestBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserDetailsForTestBuilder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public UserDetailsForTestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserDetailsForTestBuilder forename(String forename) {
            this.forename = forename;
            return this;
        }

        public UserDetailsForTestBuilder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public UserDetailsForTest build() {
            return new UserDetailsForTest(id, roles, email, forename, surname);
        }

        public String toString() {
            return "UserDetailsForTest.UserDetailsForTestBuilder(id=" + this.id + ", roles=" + this.roles + ", email=" + this.email + ", forename=" + this.forename + ", surname=" + this.surname + ")";
        }
    }
}
