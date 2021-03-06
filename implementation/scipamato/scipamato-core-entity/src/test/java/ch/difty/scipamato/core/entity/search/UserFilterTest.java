package ch.difty.scipamato.core.entity.search;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class UserFilterTest {

    @Test
    public void test() {
        UserFilter f = new UserFilter();
        f.setNameMask("name");
        f.setEmailMask("email");
        f.setEnabled(true);

        assertThat(f.getNameMask()).isEqualTo("name");
        assertThat(f.getEmailMask()).isEqualTo("email");
        assertThat(f.getEnabled()).isEqualTo(true);

        assertThat(f.toString()).isEqualTo("UserFilter(nameMask=name, emailMask=email, enabled=true)");
    }

    @Test
    public void equals() {
        EqualsVerifier
            .forClass(UserFilter.class)
            .withRedefinedSuperclass()
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public void assertEnumFields() {
        assertThat(UserFilter.UserFilterFields.values())
            .extracting("name")
            .containsExactly("nameMask", "emailMask", "enabled");
    }

}
