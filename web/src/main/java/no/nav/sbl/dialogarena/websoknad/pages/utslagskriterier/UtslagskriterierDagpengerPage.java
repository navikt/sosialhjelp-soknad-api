package no.nav.sbl.dialogarena.websoknad.pages.utslagskriterier;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.AvbruttPage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.RedirectTilKvitteringPage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;

import static java.lang.String.format;

public class UtslagskriterierDagpengerPage extends BasePage {

    @Inject
    private SendSoknadService soknadService;

    @Value("${saksoversikt.link.url}")
    private String saksoversiktUrl;

    public UtslagskriterierDagpengerPage(PageParameters parameters) {
        super(parameters);

        add(new SoknadComponent("soknad", SkjemaBootstrapFile.UTSLAGSKRITERIER_DAGPENGER));
        StringValue brukerbehandlingId = getPageParameters().get("brukerbehandlingId");

        if (!brukerbehandlingId.isEmpty()) {
            WebSoknad soknad = soknadService.hentSoknadMedBehandlingsId(brukerbehandlingId.toString());

            if (soknad.erUnderArbeid()) {
                new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(brukerbehandlingId.toString()));
            } else if (soknad.erAvbrutt()) {
                redirectToInterceptPage(new AvbruttPage(new PageParameters()));
            } else {
                SoknadStruktur struktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer());
                setResponsePage(new RedirectTilKvitteringPage(format("%s/detaljer/%s/%s", saksoversiktUrl, struktur.getTemaKode(), brukerbehandlingId)));
            }
        }
    }
}
