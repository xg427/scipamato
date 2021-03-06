package ch.difty.scipamato.core.web.model;

import static ch.difty.scipamato.common.TestUtils.assertDegenerateSupplierParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import ch.difty.scipamato.core.entity.newsletter.NewsletterTopic;
import ch.difty.scipamato.core.persistence.NewsletterTopicService;

public class NewsletterTopicModelTest extends ModelTest {

    @MockBean
    private NewsletterTopicService serviceMock;

    @Test
    public void instantiating_withNullLanguageCode_throws() {
        assertDegenerateSupplierParameter(() -> new NewsletterTopicModel(null), "languageCode");
    }

    @Test
    public void loading_delegatesToNewsletterTopicService() {
        String languageCode = "de";

        final List<NewsletterTopic> topics = new ArrayList<>();
        topics.add(new NewsletterTopic(1, "t1"));
        topics.add(new NewsletterTopic(2, "t2"));

        when(serviceMock.findAll(languageCode)).thenReturn(topics);

        final NewsletterTopicModel model = new NewsletterTopicModel("de");

        assertThat(
            extractProperty(NewsletterTopic.NewsletterTopicFields.TITLE.getName()).from(model.load())).containsExactly(
            "t1", "t2");

        verify(serviceMock).findAll(languageCode);

        verifyNoMoreInteractions(serviceMock);
    }

}