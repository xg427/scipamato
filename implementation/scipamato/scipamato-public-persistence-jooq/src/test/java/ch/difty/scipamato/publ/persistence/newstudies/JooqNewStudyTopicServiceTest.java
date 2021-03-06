package ch.difty.scipamato.publ.persistence.newstudies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.difty.scipamato.common.NullArgumentException;
import ch.difty.scipamato.common.TestUtils;
import ch.difty.scipamato.publ.entity.NewStudyTopic;

@RunWith(MockitoJUnitRunner.class)
public class JooqNewStudyTopicServiceTest {

    private static final int NL_ID = 17;

    @Mock
    private NewStudyRepository repoMock;

    @Mock
    private NewStudyTopic newStudyTopicMock;

    private JooqNewStudyTopicService service;

    private List<NewStudyTopic> studyTopics;

    @Before
    public void setUp() {
        service = new JooqNewStudyTopicService(repoMock);

        studyTopics = new ArrayList<>();
        studyTopics.add(newStudyTopicMock);
        studyTopics.add(newStudyTopicMock);
    }

    @Test(expected = NullArgumentException.class)
    public void findingMostRecentNewStudyTopics_withNullLanguage_throws() {
        service.findMostRecentNewStudyTopics(null);
    }

    @Test
    public void findingMostRecentNewStudyTopics() {
        when(repoMock.findMostRecentNewsletterId()).thenReturn(Optional.of(NL_ID));
        when(repoMock.findNewStudyTopicsForNewsletter(NL_ID, "en")).thenReturn(studyTopics);

        assertThat(service.findMostRecentNewStudyTopics("en")).containsExactly(newStudyTopicMock, newStudyTopicMock);

        verify(repoMock).findMostRecentNewsletterId();
        verify(repoMock).findNewStudyTopicsForNewsletter(NL_ID, "en");
    }

    @Test
    public void findNewStudyTopicsForNewsletterIssue_withNullIssue_throws() {
        TestUtils.assertDegenerateSupplierParameter(() -> service.findNewStudyTopicsForNewsletterIssue(null, "en"),
            "issue");
    }

    @Test
    public void findNewStudyTopicsForNewsletterIssue_withNullLanguageCode_throws() {
        TestUtils.assertDegenerateSupplierParameter(() -> service.findNewStudyTopicsForNewsletterIssue("2018/06", null),
            "languageCode");
    }

    @Test
    public void findNewStudyTopicsForNewsletterIssue() {
        when(repoMock.findIdOfNewsletterWithIssue("2018/06")).thenReturn(Optional.of(NL_ID));
        when(repoMock.findNewStudyTopicsForNewsletter(NL_ID, "en")).thenReturn(studyTopics);

        assertThat(service.findNewStudyTopicsForNewsletterIssue("2018/06", "en")).containsExactly(newStudyTopicMock,
            newStudyTopicMock);

        verify(repoMock).findIdOfNewsletterWithIssue("2018/06");
        verify(repoMock).findNewStudyTopicsForNewsletter(NL_ID, "en");
    }
}
