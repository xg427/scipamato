package ch.difty.scipamato.core.web.paper.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.table.BootstrapDefaultDataTable;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import ch.difty.scipamato.common.entity.CodeClassId;
import ch.difty.scipamato.common.persistence.paging.PaginationRequest;
import ch.difty.scipamato.core.entity.Paper;
import ch.difty.scipamato.core.entity.PaperSlimFilter;
import ch.difty.scipamato.core.entity.newsletter.PublicationStatus;
import ch.difty.scipamato.core.entity.projection.NewsletterAssociation;
import ch.difty.scipamato.core.entity.projection.PaperSlim;
import ch.difty.scipamato.core.entity.search.PaperFilter;
import ch.difty.scipamato.core.entity.search.SearchOrder;
import ch.difty.scipamato.core.persistence.CodeClassService;
import ch.difty.scipamato.core.persistence.CodeService;
import ch.difty.scipamato.core.persistence.PaperService;
import ch.difty.scipamato.core.web.common.PanelTest;
import ch.difty.scipamato.core.web.paper.AbstractPaperSlimProvider;
import ch.difty.scipamato.core.web.paper.PaperSlimBySearchOrderProvider;
import ch.difty.scipamato.core.web.paper.entry.PaperEntryPage;

public class ResultPanelTest extends PanelTest<ResultPanel> {

    private static final long   NUMBER        = 2L;
    private static final int    ROWS_PER_PAGE = 12;
    private static final String LC            = "en_us";

    @MockBean
    private CodeClassService codeClassServiceMock;
    @MockBean
    private CodeService      codeServiceMock;

    @Mock
    private SearchOrder searchOrderMock;

    private final PaperSlim paperSlim = new PaperSlim(1L, NUMBER, "firstAuthor", 2016, "title");

    @Mock
    private Paper paperMock;

    @Override
    protected void setUpHook() {
        when(paperSlimServiceMock.countBySearchOrder(searchOrderMock)).thenReturn(1);
        when(paperSlimServiceMock.findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class))).thenReturn(
            Collections.singletonList(paperSlim));

        when(paperServiceMock.findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class),
            eq(LC))).thenReturn(Collections.singletonList(paperMock));
    }

    @After
    public void tearDown() {
        // after the login
        verify(paperSlimServiceMock).countByFilter(isA(PaperFilter.class));
        verify(paperServiceMock).findPageOfIdsByFilter(isA(PaperFilter.class), isA(PaginationRequest.class));
        verifyNoMoreInteractions(paperSlimServiceMock, paperServiceMock, codeClassServiceMock, codeServiceMock,
            searchOrderMock);
    }

    @Override
    protected ResultPanel makePanel() {
        return new ResultPanel(PANEL_ID, new PaperSlimBySearchOrderProvider(searchOrderMock, ROWS_PER_PAGE)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isOfferingSearchComposition() {
                return true;
            }

            @Override
            protected GenericWebPage<Paper> getResponsePage(IModel<PaperSlim> m, String languageCode,
                PaperService paperService, AbstractPaperSlimProvider<? extends PaperSlimFilter> dataProvider) {
                return new PaperEntryPage(Model.of(paperService
                    .findByNumber(m
                        .getObject()
                        .getNumber(), languageCode)
                    .orElse(new Paper())), getPage().getPageReference(), dataProvider.getSearchOrderId(),
                    dataProvider.isShowExcluded());
            }
        };
    }

    @Override
    protected void assertSpecificComponents() {
        String b = PANEL_ID;
        getTester().assertComponent(b, Panel.class);
        String bb = b + ":table";
        getTester().assertComponent(bb, BootstrapDefaultDataTable.class);
        assertTableRow(bb + ":body:rows:1:cells");

        bb = b + ":summaryLink";
        getTester().assertComponent(bb, ResourceLink.class);
        bb = b + ":summaryShortLink";
        getTester().assertComponent(bb, ResourceLink.class);
        bb = b + ":reviewLink";
        getTester().assertComponent(bb, ResourceLink.class);
        bb = b + ":literatureReviewLink";
        getTester().assertComponent(bb, ResourceLink.class);
        bb = b + ":summaryTableLink";
        getTester().assertComponent(bb, ResourceLink.class);

        verify(paperSlimServiceMock, times(1)).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
        verify(searchOrderMock, times(2)).isShowExcluded();
    }

    private void assertTableRow(String bb) {
        getTester().assertLabel(bb + ":1:cell", "1");
        getTester().assertLabel(bb + ":2:cell", String.valueOf(NUMBER));
        getTester().assertLabel(bb + ":3:cell", "firstAuthor");
        getTester().assertLabel(bb + ":4:cell", "2016");
        getTester().assertLabel(bb + ":5:cell:link:label", "title");
        getTester().assertComponent(bb + ":6:cell:link", AjaxLink.class);
        getTester().assertLabel(bb + ":6:cell:link:image", "");
        getTester().assertLabel(bb + ":7:cell:link:image", "");
    }

    @Test
    public void clickingLink_opensPaperEntryPage() {
        Paper paper = new Paper();
        paper.setNumber(NUMBER);
        when(paperServiceMock.findByNumber(NUMBER, LC)).thenReturn(Optional.of(paper));

        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":table:body:rows:1:cells:5:cell:link");
        getTester().assertRenderedPage(PaperEntryPage.class);

        verify(paperSlimServiceMock).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
        verify(paperServiceMock).findByNumber(NUMBER, LC);
        verify(codeClassServiceMock).find(anyString());
        verify(codeServiceMock, times(8)).findCodesOfClass(isA(CodeClassId.class), anyString());
        verify(searchOrderMock).getId();
        verify(searchOrderMock, times(3)).isShowExcluded();
    }

    @Test
    public void clickingDeleteIconLink_() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":table:body:rows:1:cells:6:cell:link");
        getTester().assertComponentOnAjaxResponse(PANEL_ID + ":table");

        verify(paperSlimServiceMock, times(2)).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock, times(2)).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
        verify(searchOrderMock, times(4)).isShowExcluded();
    }

    /**
     * Note, we're partially also testing the PaperSummaryDataSource and even the
     * Provider here in order to make sure the functionality is triggered. Not sure
     * how to verify the action otherwise.
     * <p>
     * Also, this is not really asserting anything, just verifying the methods have
     * been called. Bit of a workaround
     */
    private void verifyPdfExport() {
        verify(paperSlimServiceMock, times(2)).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock, times(1)).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
        verify(paperServiceMock).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class), eq(LC));
        verify(searchOrderMock, times(2)).isShowExcluded();
    }

    @Test
    public void clickingSummaryLink_succeeds() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":summaryLink");
        verifyPdfExport();
    }

    @Test
    public void clickingSummaryShortLink_succeeds() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":summaryShortLink");
        verifyPdfExport();
    }

    @Test
    public void clickingReviewLink_succeeds() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":reviewLink");
        verifyPdfExport();
    }

    @Test
    public void clickingLiteratureReviewLink_succeeds() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":literatureReviewLink");
        verifyPdfExport();
    }

    @Test
    public void clickingSummaryTableLink_succeeds() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":summaryTableLink");
        verifyPdfExport();
    }

    @Test
    public void clickingSummaryTableWithoutResultLink_succeeds() {
        getTester().startComponentInPage(makePanel());
        getTester().clickLink(PANEL_ID + ":summaryTableWithoutResultsLink");
        verifyPdfExport();
    }

    @Test
    public void startingPage_showingResults() {
        when(searchOrderMock.isShowExcluded()).thenReturn(false);
        assertExcludeIcon("fa fa-fw fa-ban", "Exclude the paper from the search");
    }

    @Test
    public void startingPage_showingExclusions() {
        when(searchOrderMock.isShowExcluded()).thenReturn(true);
        assertExcludeIcon("fa fa-fw fa-check-circle-o", "Re-include the paper into the search");
    }

    private void assertExcludeIcon(String iconClass, String titleValue) {
        getTester().startComponentInPage(makePanel());

        String responseTxt = getTester()
            .getLastResponse()
            .getDocument();

        TagTester iconTagTester = TagTester.createTagByAttribute(responseTxt, "class", iconClass);
        assertThat(iconTagTester).isNotNull();
        assertThat(iconTagTester.getName()).isEqualTo("i");

        TagTester titleTagTester = TagTester.createTagByAttribute(responseTxt, "title", titleValue);
        assertThat(titleTagTester).isNotNull();
        assertThat(titleTagTester.getName()).isEqualTo("i");

        verify(paperSlimServiceMock, times(1)).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
        verify(searchOrderMock, times(2)).isShowExcluded();
    }

    @Test
    public void startingPage_inNonSearchContext_doesNotRenderExcludeFromSearchIcon() {
        ResultPanel panel = newNonSearchRelevantResultPanel();
        getTester().startComponentInPage(panel);

        String responseTxt = getTester()
            .getLastResponse()
            .getDocument();

        TagTester iconExcludeTagTester = TagTester.createTagByAttribute(responseTxt, "class", "fa fa-fw fa-ban");
        assertThat(iconExcludeTagTester).isNull();

        verify(paperSlimServiceMock, times(1)).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
    }

    // with isOfferingSearchComposition = false
    private ResultPanel newNonSearchRelevantResultPanel() {
        return new ResultPanel(PANEL_ID, new PaperSlimBySearchOrderProvider(searchOrderMock, ROWS_PER_PAGE)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isOfferingSearchComposition() {
                return false;
            }

            @Override
            protected GenericWebPage<Paper> getResponsePage(IModel<PaperSlim> m, String languageCode,
                PaperService paperService, AbstractPaperSlimProvider<? extends PaperSlimFilter> dataProvider) {
                return null;
            }
        };
    }

    @Test
    public void startingPage_withPaperWithNoNewsletter_rendersAddToNewsletterLink() {
        assertThat(paperSlim.getNewsletterAssociation()).isNull();
        assertNewsletterIcon("fa fa-fw fa-plus-square-o", "Add to current newsletter");
    }

    private void assertNewsletterIcon(String iconClass, String titleValue) {
        getTester().startComponentInPage(newNonSearchRelevantResultPanel());

        String responseTxt = getTester()
            .getLastResponse()
            .getDocument();

        TagTester iconTagTester = TagTester.createTagByAttribute(responseTxt, "class", iconClass);
        assertThat(iconTagTester).isNotNull();
        assertThat(iconTagTester.getName()).isEqualTo("i");

        TagTester titleTagTester = TagTester.createTagByAttribute(responseTxt, "title", titleValue);
        assertThat(titleTagTester).isNotNull();
        assertThat(titleTagTester.getName()).isEqualTo("i");

        verify(paperSlimServiceMock, times(1)).countBySearchOrder(searchOrderMock);
        verify(paperSlimServiceMock).findPageBySearchOrder(eq(searchOrderMock), isA(PaginationRequest.class));
    }

    @Test
    public void startingPage_withPaperWithNewsletter_rendersAddToNewsletterLink() {
        NewsletterAssociation ns = new NewsletterAssociation(1, "1802", PublicationStatus.PUBLISHED.getId(), null);
        paperSlim.setNewsletterAssociation(ns);
        assertNewsletterIcon("fa fa-fw fa-envelope-o", "Newsletter 1802");
    }

}
