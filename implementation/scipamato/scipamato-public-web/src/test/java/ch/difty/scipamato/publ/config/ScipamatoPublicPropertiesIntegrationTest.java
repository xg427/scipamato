package ch.difty.scipamato.publ.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ch.difty.scipamato.publ.ScipamatoPublicApplication;

/**
 * Note, this test class currently derives the configured values from
 * <literal>application.properties</literal>.
 *
 * @author u.joss
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScipamatoPublicApplication.class)
public class ScipamatoPublicPropertiesIntegrationTest {

    @Autowired
    public ApplicationPublicProperties appProperties;

    @Test
    public void gettingBuildVersion() {
        assertThat(appProperties.getBuildVersion()).matches("\\d+\\.\\d+\\.\\d+.*");
    }

    @Test
    public void assertDefaultLocalization() {
        assertThat(appProperties.getDefaultLocalization()).isEqualTo("de");
    }

    @Test
    public void assertBrand() {
        assertThat(appProperties.getBrand()).isEqualTo("SciPaMaTo");
    }

    @Test
    public void assertPubmedBaseUrl() {
        assertThat(appProperties.getPubmedBaseUrl()).isEqualTo("https://www.ncbi.nlm.nih.gov/pubmed/");
    }

    @Test
    public void assertPresenceOfCommercialFont() {
        assertThat(appProperties.isCommercialFontPresent()).isEqualTo(false);
    }

    @Test
    public void assertNavbarVisibleByDefault() {
        assertThat(appProperties.isNavbarVisibleByDefault()).isEqualTo(false);
    }

    @Test
    public void assertCmsUrlSearchPage() {
        assertThat(appProperties.getCmsUrlSearchPage()).isEqualTo("http://localhost:8081/");
    }

    @Test
    public void assertCmsUrlNewStudyPage() {
        assertThat(appProperties.getCmsUrlNewStudyPage()).isEqualTo("http://localhost:8081/new-studies");
    }

    @Test
    public void authorsAbbreviatedMaxLength() {
        assertThat(appProperties.getAuthorsAbbreviatedMaxLength()).isEqualTo(60);
    }

    @Test
    public void isResponsiveIframeSupportEnabled() {
        assertThat(appProperties.isResponsiveIframeSupportEnabled()).isFalse();
    }

}
