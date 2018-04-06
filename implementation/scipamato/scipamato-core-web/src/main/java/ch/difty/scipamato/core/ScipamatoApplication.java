package ch.difty.scipamato.core;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import com.giffing.wicket.spring.boot.starter.app.WicketBootSecuredWebApplication;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@ComponentScan(basePackages = "ch.difty.scipamato")
public class ScipamatoApplication extends WicketBootSecuredWebApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(ScipamatoApplication.class)
            .run(args);
    }

    @Override
    protected void init() {
        super.init();

        // enable putting JavaScript into Footer Container
        setHeaderResponseDecorator(r -> new JavaScriptFilteredIntoFooterHeaderResponse(r, "footer-container"));

        registerJasperJrxmlFilesWithPackageResourceGuard();
    }

    // Allow access to jrxml jasper report definition files
    private void registerJasperJrxmlFilesWithPackageResourceGuard() {
        final IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            final SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.jrxml");
        }
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new ScipamatoSession(request);
    }

}
