package no.nav.sbl.dialogarena.websoknad;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.frontend.FrontendConfigurator;
import no.nav.modig.frontend.FrontendModules;
import no.nav.modig.frontend.MetaTag;
import no.nav.modig.wicket.configuration.ApplicationSettingsConfig;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.websoknad.pages.soknadliste.SoknadListePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.StartSoknadPage;
import no.nav.sbl.dialogarena.websoknad.selftest.SelfTestPage;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;


public class WicketApplication extends WebApplication {

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private CmsContentRetriever cmsContentRetriever;

    public static WicketApplication get() {
        return (WicketApplication) Application.get();
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return SoknadListePage.class;
    }


    @Override
    protected void init() {
        super.init();
        setSpringComponentInjector();
        IMarkupSettings settings = getMarkupSettings();
        settings.setDefaultMarkupEncoding("UTF-8");

        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");

        FrontendConfigurator configurator = new FrontendConfigurator();

        for (LessResources resource : LessResources.values()) {
            configurator.addLess(resource.getResource());
        }

        for (ConditionalCssResources resource : ConditionalCssResources.values()) {
            configurator.addConditionalCss(resource.getResource(this));
        }

        for (JsResources js : JsResources.values()) {
            configurator.addScripts(js.getResource());
        }

        configurator
                .addMetas(
                        MetaTag.XUA_IE_EDGE,
                        MetaTag.CHARSET_UTF8,
                        MetaTag.VIEWPORT_SCALE_1)
                .withModules(
                        FrontendModules.UNDERSCORE,
                        FrontendModules.EKSTERNFLATE
                )
                .addLess(InnstillingerPanel.INNSTILLINGER_LESS)
                .addScripts(InnstillingerPanel.INNSTILLINGER_JS)
                .withResourcePacking(this.usesDeploymentConfig())
                .configure(this);

        // Innstillinger vi kan ha
        IApplicationSettings applicationSettings = getApplicationSettings();
        applicationSettings.setPageExpiredErrorPage(getHomePage());
        applicationSettings.setUploadProgressUpdatesEnabled(true);


        new ApplicationSettingsConfig().withExternalExceptionPages(null).configure(this);

        Application.get().getComponentPostOnBeforeRenderListeners().add(new StatelessChecker());

        get().getStoreSettings().setMaxSizePerSession(Bytes.kilobytes(500));


        Application.get().getRequestLoggerSettings().setRequestLoggerEnabled(true);

        mountPage("soknad/${soknadType}", StartSoknadPage.class);
        mountPage("internal/selftest", SelfTestPage.class);
        mountPage("soknadliste", SoknadListePage.class);

        getSecuritySettings().setEnforceMounts(true);
        getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory());
        getResourceSettings().getStringResourceLoaders().add(0, new EnonicResourceLoader(cmsContentRetriever));

        setSpringComponentInjector();
    }


    protected void setSpringComponentInjector() {
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));
    }
}