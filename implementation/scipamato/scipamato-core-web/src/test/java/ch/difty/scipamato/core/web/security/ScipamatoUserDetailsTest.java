package ch.difty.scipamato.core.web.security;

import static ch.difty.scipamato.common.entity.ScipamatoEntity.ScipamatoEntityFields.CREATED;
import static ch.difty.scipamato.common.entity.ScipamatoEntity.ScipamatoEntityFields.MODIFIED;
import static ch.difty.scipamato.core.entity.CoreEntity.CoreEntityFields.CREATOR_ID;
import static ch.difty.scipamato.core.entity.CoreEntity.CoreEntityFields.MODIFIER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;

import java.util.Arrays;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import ch.difty.scipamato.core.auth.Role;
import ch.difty.scipamato.core.entity.User;
import ch.difty.scipamato.core.web.authentication.ScipamatoUserDetails;

public class ScipamatoUserDetailsTest {

    @Test
    public void test() {
        User user = new User(1, "un", "fn", "ln", "em", "pw", true, Arrays.asList(Role.ADMIN, Role.USER));
        ScipamatoUserDetails sud = new ScipamatoUserDetails(user);

        assertThat(sud.getId()).isEqualTo(1);
        assertThat(sud.getUserName()).isEqualTo("un");
        assertThat(sud.getFirstName()).isEqualTo("fn");
        assertThat(sud.getLastName()).isEqualTo("ln");
        assertThat(sud.getEmail()).isEqualTo("em");
        assertThat(sud.getPassword()).isEqualTo("pw");
        assertThat(sud.isEnabled()).isEqualTo(true);
        assertThat(extractProperty("key").from(sud.getRoles())).containsExactly("ROLE_ADMIN", "ROLE_USER");

        assertThat(extractProperty("authority").from(sud.getAuthorities())).containsExactly("ROLE_ADMIN", "ROLE_USER");
        assertThat(sud.getUsername()).isEqualTo("un");

        // statically set
        assertThat(sud.isAccountNonExpired()).isTrue();
        assertThat(sud.isAccountNonLocked()).isTrue();
        assertThat(sud.isCredentialsNonExpired()).isTrue();

        assertThat(sud.toString()).isEqualTo(
            "ScipamatoUserDetails[roles=[ROLE_ADMIN, ROLE_USER],userName=un,firstName=fn,lastName=ln,email=em,password=pw,enabled=true,roles=[ROLE_ADMIN, ROLE_USER],id=1,createdBy=<null>,lastModifiedBy=<null>,created=<null>,lastModified=<null>,version=0]");
    }

    @Test
    public void equals() {
        EqualsVerifier
            .forClass(User.class)
            .withRedefinedSuperclass()
            .withIgnoredFields(CREATED.getName(), CREATOR_ID.getName(), MODIFIED.getName(), MODIFIER_ID.getName())
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }

}
