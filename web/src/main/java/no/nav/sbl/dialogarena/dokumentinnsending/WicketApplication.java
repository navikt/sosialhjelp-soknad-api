package no.nav.sbl.dialogarena.dokumentinnsending;

import javax.inject.Inject;

import no.nav.modig.frontend.FrontendConfigurator;
import no.nav.modig.frontend.FrontendModules;
import no.nav.modig.frontend.MetaTag;
import no.nav.modig.wicket.configuration.ApplicationSettingsConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.HomePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.StartBehandlingPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenereKvitteringPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.hjelp.HjelpPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering.InnsendingKvitteringPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingslettet.InnsendingSlettetPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.leggtilvedlegg.LeggTilVedleggPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.opplasting.OpplastingPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.session.DokumentinnsendingSession;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.slettinnsending.SlettInnsendingPage;
import no.nav.sbl.dialogarena.dokumentinnsending.resource.DokumentForhandsvisningResourceReference;
import no.nav.sbl.dialogarena.dokumentinnsending.selftest.SelfTestPage;
import no.nav.sbl.dialogarena.webkomponent.innstillinger.InnstillingerPanel;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SendSoknadServicePage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SendSoknadPage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.kvittering.KvitteringPage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.oppsummering.OppsumeringPage;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.springframework.context.ApplicationContext;


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

        mountPage("startBrukerbehandling/${brukerBehandlingId}", StartBehandlingPage.class);
        mountPage("oversikt/${brukerBehandlingId}", OversiktPage.class);
        mountPage("bekreft/${brukerBehandlingId}", BekreftelsesPage.class);
        mountPage("avbrutt/${brukerBehandlingId}", InnsendingSlettetPage.class);
        mountPage("kvittering/${brukerBehandlingId}", InnsendingKvitteringPage.class);
        mountPage("fortsettSenere/${brukerBehandlingId}", FortsettSenerePage.class);
        mountPage("kvitteringFortsettSenere/${brukerBehandlingId}", FortsettSenereKvitteringPage.class);
        mountPage("hjelp/${brukerBehandlingId}", HjelpPage.class);
        mountPage("slett/${brukerBehandlingId}", SlettInnsendingPage.class);
        mountPage("vedlegg/${brukerBehandlingId}", LeggTilVedleggPage.class);
        mountPage("opplasting/${brukerBehandlingId}/${dokumentId}", OpplastingPage.class);
        mountPage("internal/selftest", SelfTestPage.class);
        
        mountPage("sendSoknad", SendSoknadPage.class);
        mountPage("sendSoknadService", SendSoknadServicePage.class);
        mountPage("oppsumering", OppsumeringPage.class);
        mountPage("soknadKvittering", KvitteringPage.class);


        mountResource("preview/${size}/${dokumentId}/${side}/thumb.png", thumbnailRef);

        getSecuritySettings().setEnforceMounts(true);
        getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory());
        //setRootRequestMapper(new ModigCryptoMapper(getRootRequestMapper(), this));

        setSpringComponentInjector();
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new DokumentinnsendingSession(request);
    }

    protected void setSpringComponentInjector() {
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));
    }

}
