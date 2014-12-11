package no.nav.sbl.dialogarena.websoknad.pages.utslagskriterier;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.SoknadAvsluttetException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.RedirectTilKvitteringPage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

public class UtslagskriterierDagpengerPage extends BasePage {

    @Inject
    private SendSoknadService soknadService;

    @Inject
    private ConfigService configService;

    public UtslagskriterierDagpengerPage(PageParameters parameters) {
        super(parameters);

        add(new SoknadComponent("soknad", SkjemaBootstrapFile.UTSLAGSKRITERIER_DAGPENGER));
        StringValue brukerbehandlingId = getPageParameters().get("brukerbehandlingId");
        if (!brukerbehandlingId.isEmpty()) {
            try {
                soknadService.hentSoknadMedBehandlingsId(brukerbehandlingId.toString());
                new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(brukerbehandlingId.toString()));
            } catch (SoknadAvsluttetException e) {
                String mineHenvendelserUrl = configService.getValue("minehenvendelser.link.url");
                setResponsePage(new RedirectTilKvitteringPage(mineHenvendelserUrl + "?behandlingsId=" + brukerbehandlingId));
            }
        }
    }
}
