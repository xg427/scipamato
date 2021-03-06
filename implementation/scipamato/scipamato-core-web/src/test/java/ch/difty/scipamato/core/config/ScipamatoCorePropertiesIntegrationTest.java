package ch.difty.scipamato.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ch.difty.scipamato.core.ScipamatoCoreApplication;
import ch.difty.scipamato.core.logic.parsing.AuthorParserStrategy;

/**
 * Note, this test class currently derives the configured values from
 * application.properties.
 *
 * @author u.joss
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScipamatoCoreApplication.class)
public class ScipamatoCorePropertiesIntegrationTest {

    @Autowired
    public ApplicationCoreProperties appProperties;

    @Test
    public void gettingBuildVersion() {
        assertThat(appProperties.getBuildVersion()).matches("\\d+\\.\\d+\\.\\d+.*");
    }

    @Test
    public void gettingDefaultStrategy() {
        assertThat(appProperties.getAuthorParserStrategy()).isEqualTo(AuthorParserStrategy.PUBMED);
    }

    @Test
    public void assertDefaultLocalization() {
        assertThat(appProperties.getDefaultLocalization()).isEqualTo("de");
    }

    @Test
    public void assertBrand() {
        assertThat(appProperties.getBrand()).isEqualTo("SciPaMaTo - Scientific Paper Management Tool");
    }

    @Test
    public void assertMinimumPaperNumberToBeRecycled() {
        assertThat(appProperties.getMinimumPaperNumberToBeRecycled()).isEqualTo(8);
    }

    @Test
    public void assertPubmedBaseUrl() {
        assertThat(appProperties.getPubmedBaseUrl()).isEqualTo("https://www.ncbi.nlm.nih.gov/pubmed/");
    }
}
