package no.nav.sbl.dialogarena.soknad;

import no.nav.modig.frontend.FrontendConfigurator;
import no.nav.modig.frontend.FrontendModules;
import no.nav.modig.frontend.MetaTag;
import no.nav.modig.wicket.configuration.ApplicationSettingsConfig;
import no.nav.sbl.dialogarena.soknad.pages.HomePage;
import no.nav.sbl.dialogarena.soknad.selftest.SelfTestPage;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;


public class WicketApplication extends WebApplication {

    @Autowired
    private ApplicationContext applicationContext;

    public static WicketApplication get() {
        return (WicketApplication) Application.get();
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();

        FrontendConfigurator configurator = new FrontendConfigurator();

        for (LessResources resource : LessResources.values()) {
            configurator.addLess(resource.getResource());
        }

        for (JsResources js : JsResources.values()) {
            configurator.addScripts(js.getResource());
        }

        configurator
                .addMetas(
                        MetaTag.CHARSET_UTF8,
                        MetaTag.VIEWPORT_SCALE_1,
                        MetaTag.XUA_IE_EDGE)
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

        new ApplicationSettingsConfig().configure(this);

        Application.get().getComponentPostOnBeforeRenderListeners().add(new StatelessChecker());

        get().getStoreSettings().setMaxSizePerSession(Bytes.kilobytes(500));


        Application.get().getRequestLoggerSettings().setRequestLoggerEnabled(true);

        mountPage("internal/selftest", SelfTestPage.class);

        getSecuritySettings().setEnforceMounts(true);
        getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory());
        //setRootRequestMapper(new ModigCryptoMapper(getRootRequestMapper(), this));

        setSpringComponentInjector();
    }

    protected void setSpringComponentInjector() {
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));
    }
}
