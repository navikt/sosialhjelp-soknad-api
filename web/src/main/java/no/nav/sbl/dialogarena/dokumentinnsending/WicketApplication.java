package no.nav.sbl.dialogarena.dokumentinnsending;

import no.nav.modig.frontend.FrontendConfigurator;
import no.nav.modig.frontend.FrontendModules;
import no.nav.modig.frontend.MetaTag;
import no.nav.modig.wicket.configuration.ApplicationSettingsConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.HomePage;
import no.nav.sbl.dialogarena.dokumentinnsending.resource.DokumentForhandsvisningResourceReference;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SendSoknadPage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SendSoknadServicePage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SoknadListePage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.kvittering.KvitteringPage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.oppsummering.OppsumeringPage;
import no.nav.sbl.dialogarena.websoknad.pages.soknad.OpenSoknadPage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.StartSoknadPage;
import no.nav.sbl.dialogarena.websoknad.pages.templates.Dagpenger;
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
    private DokumentForhandsvisningResourceReference thumbnailRef;


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
        setSpringComponentInjector();
        IMarkupSettings settings = getMarkupSettings();
        settings.setDefaultMarkupEncoding("UTF-8");

        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");

        FrontendConfigurator configurator = new FrontendConfigurator();

        for (LessResources resource : LessResources.values()) {
            configurator.addLess(resource.getResource());
        }

        for (JsResources js : JsResources.values()) {
            configurator.addScripts(js.getResource());
        }

        for (ConditionalCssResources resource : ConditionalCssResources.values()) {
            configurator.addConditionalCss(resource.getResource(this));
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


        new ApplicationSettingsConfig().configure(this);

        Application.get().getComponentPostOnBeforeRenderListeners().add(new StatelessChecker());

        get().getStoreSettings().setMaxSizePerSession(Bytes.kilobytes(500));


        Application.get().getRequestLoggerSettings().setRequestLoggerEnabled(true);

        mountPage("startSoknad", StartSoknadPage.class);
        mountPage("apne/${soknadId}", OpenSoknadPage.class);
        mountPage("sendSoknad", SendSoknadPage.class);
        mountPage("sendSoknadService", SendSoknadServicePage.class);
        mountPage("oppsumering", OppsumeringPage.class);
        mountPage("soknadKvittering", KvitteringPage.class);
        mountPage("internal/selftest", SelfTestPage.class);

        mountPage("soknadliste", SoknadListePage.class);
        mountPage("dagpenger", Dagpenger.class);


        mountResource("preview/${size}/${dokumentId}/${side}/thumb.png", thumbnailRef);

        getSecuritySettings().setEnforceMounts(true);
        getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory());

        setSpringComponentInjector();
    }


    protected void setSpringComponentInjector() {
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));
    }
}