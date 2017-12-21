package ch.difty.scipamato.public_.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ScipamatoPropertiesTest {

    private final ScipamatoProperties sp = new ScipamatoProperties();

    @Test
    public void brand_hasDefaultValue() {
        assertThat(sp.getBrand()).isEqualTo("SciPaMaTo-Public");
    }

    @Test
    public void defaultLocalization_hasDefaultEnglish() {
        assertThat(sp.getDefaultLocalization()).isEqualTo("en");
    }

    @Test
    public void pubmedBaseUrl_hasDefaultValue() {
        assertThat(sp.getPubmedBaseUrl()).isEqualTo("https://www.ncbi.nlm.nih.gov/pubmed/");
    }

}