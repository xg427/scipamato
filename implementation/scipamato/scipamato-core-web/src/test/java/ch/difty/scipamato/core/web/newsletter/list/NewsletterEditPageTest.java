package ch.difty.scipamato.core.web.newsletter.list;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDate;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapButton;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelect;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.junit.After;
import org.junit.Test;

import ch.difty.scipamato.core.entity.newsletter.Newsletter;
import ch.difty.scipamato.core.entity.newsletter.PublicationStatus;
import ch.difty.scipamato.core.web.common.BasePageTest;
import ch.difty.scipamato.core.web.newsletter.edit.NewsletterEditPage;
import ch.difty.scipamato.core.web.paper.result.ResultPanel;

public class NewsletterEditPageTest extends BasePageTest<NewsletterEditPage> {

    private final Newsletter nl = Newsletter
        .builder()
        .issue("1804")
        .issueDate(LocalDate.parse("2018-04-01"))
        .publicationStatus(PublicationStatus.CANCELLED)
        .build();

    @After
    public void tearDown() {
        verifyNoMoreInteractions(newsletterServiceMock);
    }

    @Override
    protected NewsletterEditPage makePage() {
        return new NewsletterEditPage(Model.of(nl));
    }

    @Override
    protected Class<NewsletterEditPage> getPageClass() {
        return NewsletterEditPage.class;
    }

    @Override
    public void assertSpecificComponents() {
        String b = "form";
        getTester().assertComponent(b, Form.class);

        b += ":";
        getTester().assertLabel(b + "issueLabel", "Issue");
        getTester().assertComponent(b + "issue", TextField.class);

        getTester().assertLabel(b + "issueDateLegacyLabel", "Issue Date");
        getTester().assertComponent(b + "issueDateLegacy", DateTextField.class);

        getTester().assertLabel(b + "publicationStatusLabel", "Publication Status");
        getTester().assertComponent(b + "publicationStatus", BootstrapSelect.class);
        getTester().assertEnabled(b + "publicationStatus");

        getTester().assertComponent(b + "submit", BootstrapButton.class);

        getTester().assertComponent("resultPanel", ResultPanel.class);
    }

    @Test
    public void submitting_callsService() {
        getTester().startPage(NewsletterEditPage.class);

        FormTester formTester = getTester().newFormTester("form");
        formTester.setValue("issue", "1806");
        formTester.submit("submit");

        verify(newsletterServiceMock).saveOrUpdate(isA(Newsletter.class));
    }

    @Test
    public void callingWithoutModel_restrictsToCreatingNewWIPNewsletter() {
        getTester().startPage(new NewsletterEditPage());
        getTester().assertRenderedPage(getPageClass());

        getTester().assertDisabled("form:publicationStatus");

        getTester().assertNoErrorMessage();
        getTester().assertNoInfoMessage();
    }
}