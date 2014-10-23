package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

import static java.lang.String.format;

public class StartSoknadPage extends BasePage {

    @Inject
    private SendSoknadService soknadService;

    @Inject
    private ConfigService configService;

    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        add(new SoknadComponent("soknad", SkjemaBootstrapFile.DAGPENGER));
        StringValue brukerbehandlingId = getPageParameters().get("brukerbehandlingId");

        if (!brukerbehandlingId.isEmpty()) {
            WebSoknad soknad = soknadService.hentSoknadMedBehandlingsId(brukerbehandlingId.toString());

            if (soknad.erUnderArbeid()) {
                new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(brukerbehandlingId.toString()));
            } else if (soknad.erAvbrytt()) {
                redirectToInterceptPage(new AvbruttPage(new PageParameters()));
            } else {
                String saksoversiktUrl = configService.getValue("saksoversikt.link.url");
                SoknadStruktur struktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer());
                setResponsePage(new RedirectTilKvitteringPage(format("%s/detaljer/%s/%s", saksoversiktUrl, struktur.getTemaKode(), brukerbehandlingId)));
            }
        }
    }
}
