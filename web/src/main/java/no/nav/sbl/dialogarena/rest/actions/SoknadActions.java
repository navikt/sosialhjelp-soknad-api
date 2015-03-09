package no.nav.sbl.dialogarena.rest.actions;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.print.PDFFabrikk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.websoknad.domain.FortsettSenere;
import no.nav.sbl.dialogarena.websoknad.domain.SoknadBekreftelse;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getEttersendelseUrl;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getGjenopptaUrl;

@Path("/soknader/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
public class SoknadActions {

    private static Logger logger = LoggerFactory.getLogger(SoknadActions.class);

    private String saksoversiktUrl = System.getProperty("saksoversikt.link.url");

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private EmailService emailService;

    @Inject
    private NavMessageSource messageSource;

    @GET
    @Path("/leggved")
    @SjekkTilgangTilSoknad
    public Vedlegg leggVedVedlegg(@PathParam("behandlingsId") final String behandlingsId, @QueryParam("vedleggId") final Long vedleggId) {
        vedleggService.genererVedleggFaktum(behandlingsId, vedleggId);
        return vedleggService.hentVedlegg(vedleggId, false);
    }

    @POST
    @Path("/send")
    @SjekkTilgangTilSoknad
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknadMedFaktaOgVedlegg(behandlingsId);

        byte[] kvittering = genererPdfMedKodeverksverdier(soknad, "/skjema/kvittering");
        vedleggService.lagreKvitteringSomVedlegg(behandlingsId, kvittering);

        if (soknad.erEttersending()) {
            byte[] dummyPdfSomHovedskjema = genererPdf(soknad, "skjema/ettersending/dummy");
            soknadService.sendSoknad(behandlingsId, dummyPdfSomHovedskjema);
        } else {
            byte[] soknadPdf;
            if (soknad.erGjenopptak()) {
                soknadPdf = genererPdfMedKodeverksverdier(soknad, "/skjema/gjenopptak");
            } else {
                soknadPdf = genererPdfMedKodeverksverdier(soknad, "/skjema/dagpenger");
            }
            soknadService.sendSoknad(behandlingsId, soknadPdf);
        }
    }

    //TODO: trenger man å wrappe epost i eget objekt?
    @POST
    @Path("/fortsettsenere")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, FortsettSenere epost, @Context HttpServletRequest request) {
        String content = messageSource.getMessage("fortsettSenere.sendEpost.epostInnhold",
                new Object[]{getGjenopptaUrl(request.getRequestURL().toString(), behandlingsId)}, new Locale("nb", "NO"));
        String subject = messageSource.getMessage("fortsettSenere.sendEpost.epostTittel", null, new Locale("nb", "NO"));

        emailService.sendEpost(epost.getEpost(), subject, content, behandlingsId);
    }

    @POST
    @Path("/bekreftinnsending")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, SoknadBekreftelse soknadBekreftelse, @Context HttpServletRequest request) {
        if (soknadBekreftelse.getEpost() != null && !soknadBekreftelse.getEpost().isEmpty()) {
            String subject = messageSource.getMessage("sendtSoknad.sendEpost.epostSubject", null, new Locale("nb", "NO"));
            String ettersendelseUrl = getEttersendelseUrl(request.getRequestURL().toString(), behandlingsId);
            String saksoversiktLink = saksoversiktUrl + "/detaljer/" + soknadBekreftelse.getTemaKode() + "/" + behandlingsId;
            String innhold = messageSource.getMessage("sendtSoknad.sendEpost.epostInnhold", new Object[]{saksoversiktLink, ettersendelseUrl}, new Locale("nb", "NO"));
            if (soknadBekreftelse.getErEttersendelse()) {
                innhold = messageSource.getMessage("sendEttersendelse.sendEpost.epostInnhold", new Object[]{saksoversiktLink}, new Locale("nb", "NO"));
            }

            emailService.sendEpost(soknadBekreftelse.getEpost(), subject, innhold, behandlingsId);

        } else {
            logger.debug("Fant ingen epostadresse");
        }
    }

    @GET
    @Path("/sisteinnsending")
    @SjekkTilgangTilSoknad
    public Map<String, String> finnSisteInnsending(@PathParam("behandlingsId") String behandlingsId) {
        return soknadService.hentInnsendtDatoOgSisteInnsending(behandlingsId);
    }

    private byte[] genererPdfMedKodeverksverdier(WebSoknad soknad, String hbsSkjemaPath) {
        vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
        return genererPdf(soknad, hbsSkjemaPath);
    }

    private byte[] genererPdf(WebSoknad soknad, String hbsSkjemaPath) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return PDFFabrikk.lagPdfFil(pdfMarkup);
    }
}
