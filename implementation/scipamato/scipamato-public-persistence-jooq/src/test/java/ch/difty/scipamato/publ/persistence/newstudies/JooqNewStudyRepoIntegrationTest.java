package ch.difty.scipamato.publ.persistence.newstudies;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.difty.scipamato.publ.entity.NewStudy;
import ch.difty.scipamato.publ.entity.NewStudyTopic;
import ch.difty.scipamato.publ.persistence.JooqTransactionalIntegrationTest;

public class JooqNewStudyRepoIntegrationTest extends JooqTransactionalIntegrationTest {

    @Autowired
    private NewStudyRepository repo;

    @Test
    public void findingTopicsOfNewsletter1_inEnglish_returnsNoResults() {
        assertThat(repo.findNewStudyTopicsForNewsletter(1, "en"))
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void findingTopicsOfNewsletter1_inGerman_returnsResults() {
        List<NewStudyTopic> result = repo.findNewStudyTopicsForNewsletter(1, "de");
        assertThat(result)
            .isNotNull()
            .hasSize(4);

        assertThat(result)
            .extracting("sort")
            .containsExactly(1, 2, 3, 4);
        assertThat(result)
            .extracting("title")
            .containsExactly("Tiefe Belastungen", "Hirnleistung und neurodegenerative Erkrankungen",
                "Feinstaubkomponenten und PAK", "Hirnschlag");

        assertThat(result
            .get(0)
            .getStudies()).hasSize(2);
        assertThat(result
            .get(1)
            .getStudies()).hasSize(3);
        assertThat(result
            .get(2)
            .getStudies()).hasSize(3);
        assertThat(result
            .get(3)
            .getStudies()).hasSize(2);

        assertThat(result
            .get(0)
            .getStudies())
            .extracting("number")
            .containsExactly(8924l, 8993l);
        assertThat(result
            .get(1)
            .getStudies())
            .extracting("number")
            .containsExactly(8973l, 8983l, 8984l);
        assertThat(result
            .get(2)
            .getStudies())
            .extracting("number")
            .containsExactly(8933l, 8897l, 8861l);
        assertThat(result
            .get(3)
            .getStudies())
            .extracting("number")
            .containsExactly(8916l, 8934l);

        NewStudy ns = result
            .get(0)
            .getStudies()
            .get(0);
        assertThat(ns.getSort()).isEqualTo(1);
        assertThat(ns.getNumber()).isEqualTo(8924l);
        assertThat(ns.getYear()).isEqualTo(2017);
        assertThat(ns.getAuthors()).isEqualTo("Di et al.");
        assertThat(ns.getReference()).isEqualTo("(Di et al.; 2017)");
        assertThat(ns.getHeadline()).startsWith(
            "USA: Grosse Kohortenstudie zeigt, dass auch ein PM2.5-Grenzwert von 12");
        assertThat(ns.getDescription()).startsWith(
            "Registerkohortenstudie in den USA zur Untersuchung, ob die Sterblichkeit");
    }

    @Test
    public void findingMostRecentNewsletterId() {
        assertThat(repo.findMostRecentNewsletterId())
            .isPresent()
            .hasValue(2);
    }
}