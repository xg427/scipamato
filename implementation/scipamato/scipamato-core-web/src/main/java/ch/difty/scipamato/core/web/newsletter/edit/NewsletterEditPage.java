package ch.difty.scipamato.core.web.newsletter.edit;

import static ch.difty.scipamato.core.entity.newsletter.Newsletter.NewsletterFields.*;

import java.util.Arrays;
import java.util.Date;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelect;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCDNCSSReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.*;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import ch.difty.scipamato.core.auth.Roles;
import ch.difty.scipamato.core.entity.Paper;
import ch.difty.scipamato.core.entity.PaperSlimFilter;
import ch.difty.scipamato.core.entity.newsletter.Newsletter;
import ch.difty.scipamato.core.entity.newsletter.PublicationStatus;
import ch.difty.scipamato.core.entity.projection.PaperSlim;
import ch.difty.scipamato.core.entity.search.PaperFilter;
import ch.difty.scipamato.core.persistence.NewsletterService;
import ch.difty.scipamato.core.persistence.OptimisticLockingException;
import ch.difty.scipamato.core.persistence.PaperService;
import ch.difty.scipamato.core.web.common.BasePage;
import ch.difty.scipamato.core.web.paper.AbstractPaperSlimProvider;
import ch.difty.scipamato.core.web.paper.NewsletterChangeEvent;
import ch.difty.scipamato.core.web.paper.PaperSlimByPaperFilterProvider;
import ch.difty.scipamato.core.web.paper.entry.PaperEntryPage;
import ch.difty.scipamato.core.web.paper.result.ResultPanel;

@MountPath("newsletters/entry")
@Slf4j
@AuthorizeInstantiation({ Roles.USER, Roles.ADMIN })
public class NewsletterEditPage extends BasePage<Newsletter> {

    private static final int RESULT_PAGE_SIZE = 12;

    @SpringBean
    private NewsletterService service;

    private PaperSlimByPaperFilterProvider dataProvider;

    private boolean newNewsletter;

    public NewsletterEditPage() {
        this(null);
        newNewsletter = true;
    }

    public NewsletterEditPage(final IModel<Newsletter> model) {
        super(getModelOrDefaultModelFrom(model));
        initFilterAndProvider();
    }

    private static IModel<Newsletter> getModelOrDefaultModelFrom(final IModel<Newsletter> model) {
        if (model != null)
            return model;
        return newDefaultModel();
    }

    private static IModel<Newsletter> newDefaultModel() {
        final Newsletter newNl = new Newsletter();
        newNl.setPublicationStatus(PublicationStatus.WIP);
        return Model.of(newNl);
    }

    private void initFilterAndProvider() {
        PaperFilter filter = new PaperFilter();
        filter.setNewsletterId(getRelevantNewsletterId());
        dataProvider = new PaperSlimByPaperFilterProvider(filter, RESULT_PAGE_SIZE);
        updateNavigateable();
    }

    /**
     * Have the provider provide a list of all paper ids matching the current
     * filter. Construct a navigateable with this list and set it into the session
     */
    private void updateNavigateable() {
        getPaperIdManager().initialize(dataProvider.findAllPaperIdsByFilter());
    }

    /**
     * If we have a persisted newsletter, we use its id to filter the assigned papers.
     * A new newsletter however does not yet have an id. If we'd use the null id as filter
     * criterium, we'd get all papers back. So we need to apply a dummy newsletter id that
     * does not exist and therefore will not return any papers as assignments.
     */
    private Integer getRelevantNewsletterId() {
        return getModelObject().getId() != null ? getModelObject().getId() : -1;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(FontAwesomeCDNCSSReference.instance()));
    }

    @Override
    public void onEvent(final IEvent<?> event) {
        if (event
                .getPayload()
                .getClass() == NewsletterChangeEvent.class) {
            final AjaxRequestTarget target = ((NewsletterChangeEvent) event.getPayload()).getTarget();
            if (target != null)
                target.add(getFeedbackPanel());
            event.dontBroadcastDeeper();
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        queueForm("form");
        queueFieldAndLabel(new TextField<String>(ISSUE.getName()), new PropertyValidator<String>());
        queueFieldAndLabel(new DateTextField(ISSUE_DATE_LEGACY.getName(),
            new StringResourceModel("date.format", this, null).getString()), new PropertyValidator<Date>());
        makeAndQueuePublicationStatusSelectBox(PUBLICATION_STATUS.getName());
        queue(new BootstrapButton("submit", new StringResourceModel("submit.label"), Buttons.Type.Default));

        makeAndQueueResultPanel("resultPanel");
    }

    private void queueForm(final String id) {
        Form<Newsletter> form = new Form<Newsletter>(id, new CompoundPropertyModel<>(getModel())) {

            @Override
            protected void onSubmit() {
                super.onSubmit();
                doUpdate();
            }
        };
        queue(form);
    }

    private void makeAndQueuePublicationStatusSelectBox(final String id) {
        BootstrapSelect<PublicationStatus> publicationStatus = new BootstrapSelect<PublicationStatus>(id,
            new PropertyModel(getModel(), PUBLICATION_STATUS.getName()), Arrays.asList(PublicationStatus.values()),
            new EnumChoiceRenderer<>(this)) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.setEnabled(!newNewsletter);
            }
        };
        queue(publicationStatus);
        queue(new Label(id + LABEL_TAG, new StringResourceModel(id + LABEL_RESOURCE_TAG, this, null)));
    }

    private void doUpdate() {
        try {
            Newsletter persisted = service.saveOrUpdate(getModelObject());
            if (persisted != null) {
                setModelObject(persisted);
            } else {
                error(new StringResourceModel("save.error.hint", this, null)
                    .setParameters(getNullSafeId(), "")
                    .getString());
            }
        } catch (OptimisticLockingException ole) {
            final String msg = new StringResourceModel("save.optimisticlockexception.hint", this, null)
                .setParameters(ole.getTableName(), getNullSafeId())
                .getString();
            log.error(msg);
            error(msg);
        } catch (Exception ex) {
            String msg = new StringResourceModel("save.error.hint", this, null)
                .setParameters(getNullSafeId(), ex.getMessage())
                .getString();
            log.error(msg);
            error(msg);
        }
    }

    private long getNullSafeId() {
        return getModelObject().getId() != null ? getModelObject().getId() : 0L;
    }

    private void makeAndQueueResultPanel(String id) {
        ResultPanel resultPanel = new ResultPanel(id, dataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isOfferingSearchComposition() {
                return false;
            }

            @Override
            protected PaperEntryPage getResponsePage(IModel<PaperSlim> m, String languageCode,
                PaperService paperService, AbstractPaperSlimProvider<? extends PaperSlimFilter> dataProvider) {
                return new PaperEntryPage(Model.of(paperService
                    .findByNumber(m
                        .getObject()
                        .getNumber(), languageCode)
                    .orElse(new Paper())), getPage().getPageReference(), dataProvider.getSearchOrderId(),
                    dataProvider.isShowExcluded());
            }

        };
        resultPanel.setOutputMarkupId(true);
        queue(resultPanel);
    }
}
