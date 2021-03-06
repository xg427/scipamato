package ch.difty.scipamato.core.web.authentication;

import com.giffing.wicket.spring.boot.context.scan.WicketSignInPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import ch.difty.scipamato.common.web.AbstractPage;
import ch.difty.scipamato.common.web.pages.login.AbstractLoginPage;
import ch.difty.scipamato.core.web.paper.list.PaperListPage;

@MountPath("login")
@WicketSignInPage
public class LoginPage extends AbstractLoginPage {

    private static final long serialVersionUID = 1L;

    public LoginPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected AbstractPage<?> getResponsePage() {
        return new PaperListPage(getPageParameters());
    }

}
