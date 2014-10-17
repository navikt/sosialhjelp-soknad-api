package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.SoknadAvbruttException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.SoknadAvsluttetException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

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
            try {
                // TODO: Burde sikkert fikse dette
                // Henter søknaden for å triggere populering fra henvendelse, som kan kaste exceptions
                soknadService.hentSoknadMedBehandlingsId(brukerbehandlingId.toString());
                new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(brukerbehandlingId.toString()));
            } catch (SoknadAvsluttetException e) {
                String mineHenvendelserUrl = configService.getValue("minehenvendelser.link.url");
                setResponsePage(new RedirectTilKvitteringPage(mineHenvendelserUrl + "?behandlingsId=" + brukerbehandlingId));
            } catch (SoknadAvbruttException e) {
                redirectToInterceptPage(new AvbruttPage(new PageParameters()));
            }
        }
    }
}
