package ch.difty.sipamato.web.pages.paper.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import ch.difty.sipamato.entity.Paper;
import ch.difty.sipamato.entity.PaperFilter;
import ch.difty.sipamato.web.component.table.column.ClickablePropertyColumn;
import ch.difty.sipamato.web.pages.BasePage;
import ch.difty.sipamato.web.pages.paper.entry.PaperEntryPage;
import ch.difty.sipamato.web.pages.paper.provider.SortablePaperProvider;
import de.agilecoders.wicket.core.markup.html.bootstrap.table.TableBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.table.BootstrapDefaultDataTable;

@MountPath("list")
@AuthorizeInstantiation({ "ROLE_USER" })
public class PaperListPage extends BasePage<Paper> {

    private static final long serialVersionUID = 1L;

    private PaperFilter filter;

    public PaperListPage(PageParameters parameters) {
        super(parameters);
        initDefaultModel();
    }

    private void initDefaultModel() {
        filter = new PaperFilter();
    }

    protected void onInitialize() {
        super.onInitialize();

        final SortablePaperProvider dataProvider = new SortablePaperProvider(filter);

        queueFilterForm("searchForm", dataProvider);
        queueDataTable("table", dataProvider);
        queueFieldAndLabel(new TextField<String>("searchField", PropertyModel.of(dataProvider, "filterState." + PaperFilter.SEARCH_MASK)), Optional.empty());
    }

    private void queueFilterForm(final String id, final SortablePaperProvider dataProvider) {
        FilterForm<PaperFilter> form = new FilterForm<PaperFilter>(id, dataProvider);
        queue(form);
    }

    private void queueDataTable(final String id, final SortablePaperProvider dataProvider) {
        final DataTable<Paper, String> table = new BootstrapDefaultDataTable<>(id, makeTableColumns(), dataProvider, 20);
        table.setOutputMarkupId(true);
        table.add(new TableBehavior().striped().hover());
        queue(table);
    }

    private List<IColumn<Paper, String>> makeTableColumns() {
        final List<IColumn<Paper, String>> columns = new ArrayList<>();
        columns.add(makePropertyColumn("id", Paper.ID, Paper.ID));
        columns.add(makePropertyColumn("firstAuthor", Paper.FIRST_AUTHOR, Paper.FIRST_AUTHOR));
        columns.add(makePropertyColumn("publicationYear", Paper.PUBL_YEAR, Paper.PUBL_YEAR));
        columns.add(new ClickablePropertyColumn<Paper, String>(new StringResourceModel("column.header.title", this, null), Paper.TITLE, Paper.TITLE) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onClick(IModel<Paper> clicked) {
                setResponsePage(new PaperEntryPage(clicked));
            }

        });
        return columns;
    }

    private PropertyColumn<Paper, String> makePropertyColumn(String colResourceId, String propExpression, String sortProperty) {
        return new PropertyColumn<Paper, String>(new StringResourceModel("column.header." + colResourceId, this, null), sortProperty, propExpression);
    }

}
