package ch.difty.scipamato.publ.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.difty.scipamato.common.config.MavenProperties;
import ch.difty.scipamato.publ.config.ScipamatoProperties;
import ch.difty.scipamato.publ.config.ScipamatoPublicProperties;

@RunWith(MockitoJUnitRunner.class)
public class ScipamatoPublicPropertiesTest {

    private ScipamatoPublicProperties prop;

    @Mock
    private ScipamatoProperties scipamatoPropMock;
    @Mock
    private MavenProperties     mavenPropMock;

    @Before
    public void setUp() {
        prop = new ScipamatoPublicProperties(scipamatoPropMock, mavenPropMock);

        when(scipamatoPropMock.getBrand()).thenReturn("brand");
        when(scipamatoPropMock.getDefaultLocalization()).thenReturn("dl");
        when(scipamatoPropMock.getPubmedBaseUrl()).thenReturn("pbUrl");
        when(scipamatoPropMock.getRedirectFromPort()).thenReturn(5678);

        when(mavenPropMock.getVersion()).thenReturn("0.0.1-SNAPSHOT");
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(scipamatoPropMock, mavenPropMock);
    }

    @Test
    public void gettingBrand_delegatesToScipamatoProps() {
        assertThat(prop.getBrand()).isEqualTo("brand");
        verify(scipamatoPropMock).getBrand();
    }

    @Test
    public void gettingDefaultLocalization_delegatesToScipamatoProps() {
        assertThat(prop.getDefaultLocalization()).isEqualTo("dl");
        verify(scipamatoPropMock).getDefaultLocalization();
    }

    @Test
    public void gettingPubmedBaseUrl_delegatesToScipamatoProps() {
        assertThat(prop.getPubmedBaseUrl()).isEqualTo("pbUrl");
        verify(scipamatoPropMock).getPubmedBaseUrl();
    }

    @Test
    public void gettingRedirectFromPort_delegatesToScipamatoProp() {
        assertThat(prop.getRedirectFromPort()).isEqualTo(5678);
        verify(scipamatoPropMock).getRedirectFromPort();
    }

    @Test
    public void gettingBuildVersion_delegatesToMavenProp() {
        assertThat(prop.getBuildVersion()).isEqualTo("0.0.1-SNAPSHOT");
        verify(mavenPropMock).getVersion();
    }

}