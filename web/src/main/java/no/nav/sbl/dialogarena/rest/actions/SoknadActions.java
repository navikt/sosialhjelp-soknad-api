package no.nav.sbl.dialogarena.rest.actions;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.rest.meldinger.FortsettSenere;
import no.nav.sbl.dialogarena.rest.meldinger.SoknadBekreftelse;
import no.nav.sbl.dialogarena.service.EmailService;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.utils.PDFFabrikk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getEttersendelseUrl;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getFortsettUrl;

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
    private NavMessageSource tekster;

    @Context
    private ServletContext servletContext;

    @GET
    @Path("/leggved")
    @SjekkTilgangTilSoknad
    public Vedlegg leggVedVedlegg(@PathParam("behandlingsId") final String behandlingsId, @QueryParam("vedleggId") final Long vedleggId) {
        vedleggService.genererVedleggFaktum(behandlingsId, vedleggId);
        return vedleggService.hentVedlegg(vedleggId);
    }

    @POST
    @Path("/send")
    @SjekkTilgangTilSoknad
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);

        byte[] kvittering = genererPdfMedKodeverksverdier(soknad, "/skjema/kvittering");
        vedleggService.lagreKvitteringSomVedlegg(behandlingsId, kvittering);

        if (soknad.erEttersending()) {
            byte[] dummyPdfSomHovedskjema = genererPdf(soknad, "skjema/ettersending/dummy");
            soknadService.sendSoknad(behandlingsId, dummyPdfSomHovedskjema);
        } else {
            byte[] soknadPdf;
            String oppsummeringSti = "/skjema/" + soknad.getSoknadPrefix();
            soknadPdf = genererPdfMedKodeverksverdier(soknad, oppsummeringSti);
            soknadService.sendSoknad(behandlingsId, soknadPdf);
        }
    }

    @POST
    @Path("/fortsettsenere")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, FortsettSenere epost, @Context HttpServletRequest request) {
        String content = tekster.finnTekst("fortsettSenere.sendEpost.epostInnhold", new Object[]{getFortsettUrl(behandlingsId)}, new Locale("nb", "NO"));
        String subject = tekster.finnTekst("fortsettSenere.sendEpost.epostTittel", null, new Locale("nb", "NO"));

        emailService.sendEpost(epost.getEpost(), subject, content, behandlingsId);
    }

    @POST
    @Path("/bekreftinnsending")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, SoknadBekreftelse soknadBekreftelse, @Context HttpServletRequest request) {
        if (soknadBekreftelse.getEpost() != null && !soknadBekreftelse.getEpost().isEmpty()) {
            String subject = tekster.finnTekst("sendtSoknad.sendEpost.epostSubject", null, new Locale("nb", "NO"));
            String ettersendelseUrl = getEttersendelseUrl(request.getRequestURL().toString(), behandlingsId);
            String saksoversiktLink = saksoversiktUrl + "/detaljer/" + soknadBekreftelse.getTemaKode() + "/" + behandlingsId;

            String innhold;
            if (soknadBekreftelse.getErEttersendelse()) {
                innhold = tekster.finnTekst("sendEttersendelse.sendEpost.epostInnhold", new Object[]{saksoversiktLink}, new Locale("nb", "NO"));
            } else {
                innhold = tekster.finnTekst("sendtSoknad.sendEpost.epostInnhold", new Object[]{saksoversiktLink, ettersendelseUrl}, new Locale("nb", "NO"));
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

        String skjemaPath = new File(servletContext.getRealPath("/")).toURI().toString();

        return PDFFabrikk.lagPdfFil(pdfMarkup, skjemaPath);
    }
}
