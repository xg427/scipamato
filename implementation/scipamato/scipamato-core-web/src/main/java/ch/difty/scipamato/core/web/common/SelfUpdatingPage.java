package ch.difty.scipamato.core.web.common;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ch.difty.scipamato.core.config.ApplicationCoreProperties;

/**
 * Abstract page that enables the implementing concrete pages to auto-update the
 * model.
 * <p>
 * Offers the implementing pages to limit feedback messages to one message only
 * or set it back to showing all feedback messages without filtering. This can
 * be helpful if validations prevent saving a new entity {@code T} which does
 * not yet have all required fields or does not pass validation until more data
 * has been entered. See
 * {@link #tuneDownFeedbackMessages()}/{@link #resetFeedbackMessages()}.
 * <p>
 * Requires the concrete page or it's panel to add a derivative of
 * {@link AbstractAjaxTimerBehavior} to trigger the auto-functionality.
 *
 * @param <T>
 *     the type of the entity the implementing pages use as the model.
 * @author u.joss
 */
public abstract class SelfUpdatingPage<T> extends BasePage<T> {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private ApplicationCoreProperties applicationProperties;

    public SelfUpdatingPage(PageParameters parameters) {
        super(parameters);
    }

    public SelfUpdatingPage(IModel<T> model) {
        super(model);
    }

    protected ApplicationCoreProperties getApplicationProperties() {
        return applicationProperties;
    }

    /**
     * Made this final in order to enforce #addFieldBehavior to come after form
     * component initialization. Override #implSpecificOnInitialize in the pages
     * inheriting from {@link SelfUpdatingPage}
     */
    @Override
    protected final void onInitialize() {
        super.onInitialize();

        implSpecificOnInitialize();

        activateSelfUpdatingBehavior();
    }

    private void activateSelfUpdatingBehavior() {
        getForm().add(new AjaxFormValidatingBehavior("change") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onAfterSubmit(AjaxRequestTarget target) {
                super.onAfterSubmit(target);
                send(getPage(), Broadcast.BREADTH, new SelfUpdateEvent(target));
            }
        });
    }

    /**
     * pages extending {@link SelfUpdatingPage} can override this method to do what
     * they usually did in onInitialize.
     */
    protected void implSpecificOnInitialize() {
    }

    /**
     * Override to provide the form
     *
     * @return Form of type {@code T}
     */
    protected abstract Form<T> getForm();

    /**
     * Indicates the entity {@code T} has not been persisted. Turns down the number
     * of feedback messages (one message only) in order not to flood the user with
     * too much information.
     */
    protected void tuneDownFeedbackMessages() {
        getFeedbackPanel().setMaxMessages(1);
    }

    /**
     * Indicates the entity {@code T} has successfully been persisted. Turns back on
     * feedback messages to the max value, so further validation issues can be
     * indicated to the user in full detail.
     */
    protected void resetFeedbackMessages() {
        getFeedbackPanel().setMaxMessages(Integer.MAX_VALUE);
    }

}
