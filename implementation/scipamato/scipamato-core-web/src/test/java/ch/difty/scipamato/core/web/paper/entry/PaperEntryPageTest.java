package ch.difty.scipamato.core.web.paper.entry;

import static ch.difty.scipamato.core.entity.Paper.PaperFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.ClientSideBootstrapTabbedPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import ch.difty.scipamato.core.entity.Paper;
import ch.difty.scipamato.core.logic.parsing.AuthorParserFactory;
import ch.difty.scipamato.core.persistence.CodeClassService;
import ch.difty.scipamato.core.persistence.CodeService;
import ch.difty.scipamato.core.persistence.OptimisticLockingException;
import ch.difty.scipamato.core.persistence.OptimisticLockingException.Type;
import ch.difty.scipamato.core.web.common.SelfUpdatingPageTest;
import ch.difty.scipamato.core.web.paper.common.PaperPanel;

public class PaperEntryPageTest extends SelfUpdatingPageTest<PaperEntryPage> {

    @MockBean
    private AuthorParserFactory authorParserFactoryMock;

    @MockBean
    private CodeService codeServiceMock;

    @MockBean
    private CodeClassService codeClassServiceMock;

    @Mock
    private Paper persistedPaperMock;

    @Override
    protected PaperEntryPage makePage() {
        return new PaperEntryPage(Model.of(new Paper()), null);
    }

    @Override
    protected Class<PaperEntryPage> getPageClass() {
        return PaperEntryPage.class;
    }

    @Override
    protected void assertSpecificComponents() {
        String b = "contentPanel";
        getTester().assertComponent(b, PaperPanel.class);

        b += ":form";
        getTester().assertComponent(b, Form.class);

        assertLabeledTextArea(b, AUTHORS.getName());
        assertLabeledTextField(b, FIRST_AUTHOR.getName());
        assertLabeledCheckBoxX(b, FIRST_AUTHOR_OVERRIDDEN.getName());
        assertLabeledTextArea(b, TITLE.getName());
        assertLabeledTextField(b, LOCATION.getName());

        assertLabeledTextField(b, Paper.IdScipamatoEntityFields.ID.getName());
        assertLabeledTextField(b, PUBL_YEAR.getName());
        assertLabeledTextField(b, PMID.getName());
        assertLabeledTextField(b, DOI.getName());

        b += ":tabs";
        getTester().assertComponent(b, ClientSideBootstrapTabbedPanel.class);
        b += ":panelsContainer:panels";
        assertTabPanelFields(1, 1, b, GOALS.getName(), POPULATION.getName(), METHODS.getName(),
            POPULATION_PLACE.getName(), POPULATION_PARTICIPANTS.getName(), POPULATION_DURATION.getName(),
            EXPOSURE_POLLUTANT.getName(), EXPOSURE_ASSESSMENT.getName(), METHOD_STUDY_DESIGN.getName(),
            METHOD_OUTCOME.getName(), METHOD_STATISTICS.getName(), METHOD_CONFOUNDERS.getName());
        assertTabPanelFields(2, 3, b, RESULT.getName(), COMMENT.getName(), INTERN.getName(),
            RESULT_EXPOSURE_RANGE.getName(), RESULT_EFFECT_ESTIMATE.getName(), RESULT_MEASURED_OUTCOME.getName());
        assertTabPanelFieldsOfTab3(5, b, MAIN_CODE_OF_CODECLASS1.getName(), "codesClass1", "codesClass2", "codesClass3",
            "codesClass4", "codesClass5", "codesClass6", "codesClass7", "codesClass8");
        assertTabPanelFields(4, 7, b, POPULATION_PLACE.getName(), POPULATION_PARTICIPANTS.getName(),
            POPULATION_DURATION.getName(), EXPOSURE_POLLUTANT.getName(), EXPOSURE_ASSESSMENT.getName(),
            METHOD_STUDY_DESIGN.getName(), METHOD_OUTCOME.getName(), METHOD_STATISTICS.getName(),
            METHOD_CONFOUNDERS.getName(), RESULT_EXPOSURE_RANGE.getName(), RESULT_EFFECT_ESTIMATE.getName(),
            RESULT_MEASURED_OUTCOME.getName());
        assertTabPanelFields(5, 9, b, ORIGINAL_ABSTRACT.getName());
        assertTabPanelFields(6, 11, b);
    }

    private void assertTabPanelFields(int tabId, int panelId, String b, String... fields) {
        assertTabPanel(panelId, b);
        final String bb = b + ":" + panelId + ":tab" + tabId + "Form";
        getTester().assertComponent(bb, Form.class);
        for (String f : fields) {
            assertLabeledTextArea(bb, f);
        }
    }

    private void assertTabPanelFieldsOfTab3(int panelId, String b, String... fields) {
        assertTabPanel(panelId, b);
        final String bb = b + ":" + panelId + ":tab3Form";
        getTester().assertComponent(bb, Form.class);
        int fieldCount = 0;
        for (String f : fields) {
            if (fieldCount++ == 0) {
                assertLabeledTextField(bb, f);
            } else {
                assertLabeledMultiSelect(bb, f);
            }
        }
    }

    private void assertTabPanel(int i, String b) {
        final String bb = ":" + i;
        getTester().assertComponent(b + bb, Panel.class);
    }

    @Test
    public void submitting_shouldActuallyKickoffOnSubmitInTest() {
        when(paperServiceMock.saveOrUpdate(isA(Paper.class))).thenReturn(persistedPaperMock);

        getTester().startPage(makePage());
        FormTester formTester = makeSavablePaperTester();
        formTester.submit();

        getTester().assertNoInfoMessage();
        getTester().assertNoErrorMessage();
        verify(paperServiceMock).saveOrUpdate(isA(Paper.class));
    }

    @Test
    public void paperFailingValidation_showsAllValidationMessages() {
        getTester().startPage(makePage());
        applyTestHackWithNestedMultiPartForms();
        getTester().submitForm("contentPanel:form");
        getTester().assertErrorMessages("'Authors' is required.", "'Title' is required.", "'Location' is required.",
            "'Pub. Year' is required.", "'No.' is required.", "'Goals' is required.");
    }

    // See https://issues.apache.org/jira/browse/WICKET-2790
    private void applyTestHackWithNestedMultiPartForms() {
        MockHttpServletRequest servletRequest = getTester().getRequest();
        servletRequest.setUseMultiPartContentType(true);
    }

    @Test
    public void serviceThrowingError() {
        when(paperServiceMock.saveOrUpdate(isA(Paper.class))).thenThrow(new RuntimeException("foo"));

        getTester().startPage(makePage());

        FormTester formTester = makeSavablePaperTester();
        formTester.submit();

        getTester().assertErrorMessages("An unexpected error occurred when trying to save Paper [id 0]: foo");
        verify(paperServiceMock).saveOrUpdate(isA(Paper.class));
    }

    private FormTester makeSavablePaperTester() {
        FormTester formTester = getTester().newFormTester("contentPanel:form");
        formTester.setValue("number", "100");
        formTester.setValue("authors", "Poe EA.");
        formTester.setValue("title", "Title");
        formTester.setValue("location", "loc");
        formTester.setValue("publicationYear", "2017");
        formTester.setValue("tabs:panelsContainer:panels:1:tab1Form:goals", "goals");
        return formTester;
    }

    @Test
    public void serviceThrowingOptimisticLockingException() {
        when(paperServiceMock.saveOrUpdate(isA(Paper.class))).thenThrow(
            new OptimisticLockingException("paper", "rcd", Type.UPDATE));

        getTester().startPage(makePage());

        FormTester formTester = makeSavablePaperTester();
        formTester.submit();

        getTester().assertErrorMessages(
            "The paper with id 0 has been modified concurrently by another user. Please reload it and apply your changes once more.");
        verify(paperServiceMock).saveOrUpdate(isA(Paper.class));
    }

    @Test
    public void serviceReturningNullPaperAfterSave_hasErrorMessage() {
        when(paperServiceMock.saveOrUpdate(isA(Paper.class))).thenReturn(null);

        getTester().startPage(makePage());
        FormTester formTester = makeSavablePaperTester();
        formTester.submit();

        getTester().assertNoInfoMessage();
        getTester().assertErrorMessages("An unexpected error occurred when trying to save Paper [id 0]: ");

        verify(paperServiceMock).saveOrUpdate(isA(Paper.class));
    }

    @Test
    public void defaultModel_containsNaValuesAndCanSubmitWithoutErrors() {
        when(paperServiceMock.saveOrUpdate(isA(Paper.class))).thenReturn(persistedPaperMock);
        when(paperServiceMock.findLowestFreeNumberStartingFrom(7L)).thenReturn(19L);

        getTester().startPage(new PaperEntryPage(new PageParameters(), null));

        FormTester formTester = getTester().newFormTester("contentPanel:form");

        assertThat(formTester.getTextComponentValue(NUMBER.getName())).isNotNull();
        assertThat(formTester.getTextComponentValue(AUTHORS.getName())).isNotNull();
        assertThat(formTester.getTextComponentValue(FIRST_AUTHOR.getName())).isNotNull();
        assertThat(formTester.getTextComponentValue(TITLE.getName())).isNotNull();
        assertThat(formTester.getTextComponentValue(LOCATION.getName())).isNotNull();
        assertThat(formTester.getTextComponentValue(PUBL_YEAR.getName())).isNotNull();
        assertThat(formTester.getTextComponentValue("tabs:panelsContainer:panels:1:tab1Form:goals")).isNotNull();

        formTester.submit();

        getTester().assertNoInfoMessage();
        getTester().assertNoErrorMessage();
        verify(paperServiceMock).saveOrUpdate(isA(Paper.class));
        verify(paperServiceMock).findLowestFreeNumberStartingFrom(7L);
    }

}
