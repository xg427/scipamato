package ch.difty.sipamato.web.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import ch.difty.sipamato.config.ApplicationProperties;
import ch.difty.sipamato.config.AuthorParserStrategy;
import ch.difty.sipamato.service.Localization;

public abstract class SelfUpdatingPageTest<T extends BasePage<?>> extends BasePageTest<T> {

    @MockBean
    private ApplicationProperties applicationProperties;

    @MockBean
    private Localization localization;

    protected ApplicationProperties getAppProps() {
        return applicationProperties;
    }

    protected Localization getLocalization() {
        return localization;
    }

    @Override
    protected final void setUpHook() {
        when(applicationProperties.getAuthorParserStrategy()).thenReturn(AuthorParserStrategy.DEFAULT);
        when(applicationProperties.getDefaultLocalization()).thenReturn("de");
        when(applicationProperties.getBrand()).thenReturn("SiPaMaTo");
        when(localization.getLocalization()).thenReturn("de");
    }

    @Test
    public void renderedPage_setsOutputMarkupIdToComponents() {
        getTester().startPage(makePage());
        assertThat(getTester().getComponentFromLastRenderedPage("contentPanel:form:title").getOutputMarkupId()).isTrue();
    }

}