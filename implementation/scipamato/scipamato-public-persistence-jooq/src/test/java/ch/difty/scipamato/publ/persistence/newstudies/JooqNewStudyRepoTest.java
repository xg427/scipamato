package ch.difty.scipamato.publ.persistence.newstudies;

import static ch.difty.scipamato.common.TestUtils.assertDegenerateSupplierParameter;

import org.jooq.DSLContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JooqNewStudyRepoTest {

    @Mock
    private DSLContext dslContextMock;

    private JooqNewStudyRepo repo;

    @Before
    public void setUp() {
        repo = new JooqNewStudyRepo(dslContextMock);
    }

    @Test
    public void findingCodesOfClass_withNullCodeClassId_throws() {
        assertDegenerateSupplierParameter(() -> repo.findNewStudyTopicsForNewsletter(1, null), "languageCode");
    }

    @Test
    public void findIdOfNewsletterWithIssue_withNullIssue() {
        assertDegenerateSupplierParameter(() -> repo.findIdOfNewsletterWithIssue(null), "issue");
    }

}