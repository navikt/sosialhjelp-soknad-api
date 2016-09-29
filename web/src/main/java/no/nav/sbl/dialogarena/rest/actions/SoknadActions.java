package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.rest.meldinger.FortsettSenere;
import no.nav.sbl.dialogarena.rest.meldinger.SoknadBekreftelse;
import no.nav.sbl.dialogarena.rest.utils.PDFService;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.service.EmailService;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Locale;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getEttersendelseUrl;
import static no.nav.sbl.dialogarena.utils.UrlUtils.getFortsettUrl;

@Controller
@Path("/soknader/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
public class SoknadActions {

    private static Logger logger = LoggerFactory.getLogger(SoknadActions.class);

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private PDFService pdfService;

    @Inject
    private EmailService emailService;

    @Inject
    private NavMessageSource tekster;

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
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);
        String servletPath = servletContext.getRealPath("/");

        byte[] kvittering = pdfService.genererKvitteringPdf(soknad, servletPath);
        vedleggService.lagreKvitteringSomVedlegg(behandlingsId, kvittering);

        if (soknad.erEttersending()) {
            byte[] dummyPdfSomHovedskjema = pdfService.genererEttersendingPdf(soknad, servletPath);
            soknadService.sendSoknad(behandlingsId, dummyPdfSomHovedskjema);
        } else {
            byte[] soknadPdf = pdfService.genererOppsummeringPdf(soknad, servletPath);
            soknadService.sendSoknad(behandlingsId, soknadPdf);
        }
    }

    @POST
    @Path("/fortsettsenere")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, FortsettSenere epost, @Context HttpServletRequest request) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, false);
        Locale sprak = soknad.getSprak();
        String content = tekster.finnTekst("fortsettSenere.sendEpost.epostInnhold", new Object[]{getFortsettUrl(behandlingsId)}, sprak);
        String subject = tekster.finnTekst("fortsettSenere.sendEpost.epostTittel", null, sprak);

        emailService.sendEpost(epost.getEpost(), subject, content, behandlingsId);
    }

    @POST
    @Path("/bekreftinnsending")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId,
                          @DefaultValue("nb_NO") @QueryParam("sprak") String sprakkode,
                          SoknadBekreftelse soknadBekreftelse,
                          @Context HttpServletRequest request) {
        if (soknadBekreftelse.getEpost() != null && !soknadBekreftelse.getEpost().isEmpty()) {
            String saksoversiktUrl = System.getProperty("saksoversikt.link.url");
            Locale sprak = LocaleUtils.toLocale(sprakkode);
            String subject = tekster.finnTekst("sendtSoknad.sendEpost.epostSubject", null, sprak);
            String ettersendelseUrl = soknadBekreftelse.getErSoknadsdialog() ?
                    getEttersendelseUrl(request.getRequestURL().toString(), behandlingsId) :
                    saksoversiktUrl + "/app/ettersending";
            String saksoversiktLink = saksoversiktUrl + "/app/tema/" + soknadBekreftelse.getTemaKode();

            if (!sprak.equals(LocaleUtils.toLocale("nb_NO"))) {
                ettersendelseUrl += "?sprak=" + sprakkode;
                saksoversiktLink += "?sprak=" + sprakkode;
            }

            String innhold;
            if (soknadBekreftelse.getErEttersendelse()) {
                innhold = tekster.finnTekst("sendEttersendelse.sendEpost.epostInnhold", new Object[]{saksoversiktLink}, sprak);
            } else {
                innhold = tekster.finnTekst("sendtSoknad.sendEpost.epostInnhold", new Object[]{saksoversiktLink, ettersendelseUrl}, sprak);
            }

            emailService.sendEpost(soknadBekreftelse.getEpost(), subject, innhold, behandlingsId);
        } else {
            logger.debug("Fant ingen epostadresse");
        }
    }

    @GET
    @Path("/opprinneliginnsendtdato")
    @Produces(TEXT_PLAIN)
    @SjekkTilgangTilSoknad
    public Long finnOpprinneligInnsendtDato(@PathParam("behandlingsId") String behandlingsId) {
        return soknadService.hentOpprinneligInnsendtDato(behandlingsId);
    }

    @GET
    @Path("/sistinnsendtebehandlingsid")
    @Produces(TEXT_PLAIN)
    @SjekkTilgangTilSoknad
    public String finnSisteInnsendteBehandlingsId(@PathParam("behandlingsId") String behandlingsId) {
        return soknadService.hentSisteInnsendteBehandlingsId(behandlingsId);
    }

}
