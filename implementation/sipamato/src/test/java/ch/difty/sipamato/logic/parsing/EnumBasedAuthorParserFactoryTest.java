package ch.difty.sipamato.logic.parsing;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.difty.sipamato.config.ApplicationProperties;
import ch.difty.sipamato.config.AuthorParserStrategies;
import ch.difty.sipamato.lib.NullArgumentException;

@RunWith(MockitoJUnitRunner.class)
public class EnumBasedAuthorParserFactoryTest {

    private AuthorParserFactory factory;

    @Mock
    private ApplicationProperties appProperties;

    @Before
    public void setUp() {
        when(appProperties.getAuthorParserStrategy()).thenReturn(AuthorParserStrategies.DEFAULT);
        factory = new EnumBasedAuthorParserFactory(appProperties);
    }

    @After
    public void tearDown() {
        verify(appProperties).getAuthorParserStrategy();
        verifyNoMoreInteractions(appProperties);
    }

    @Test(expected = NullArgumentException.class)
    public void degenerateConstruction() {
        new EnumBasedAuthorParserFactory(null);
    }

    @Test(expected = NullArgumentException.class)
    public void creatingParser_withNullAuthorString_throws() {
        factory.createParser(null);
    }

    @Test
    public void cratingParser_withNoSetting_usesDefaultAuthorParser() {
        AuthorParser parser = factory.createParser("Turner MC.");
        assertThat(parser).isInstanceOf(DefaultAuthorParser.class);
        verify(appProperties).getAuthorParserStrategy();
    }

}