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
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Locale;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.websoknad.servlet.UrlUtils.getEttersendelseUrl;
import static no.nav.sbl.dialogarena.websoknad.servlet.UrlUtils.getGjenopptaUrl;

@Path("/soknad/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
public class SoknadActions {

    private static Logger logger = LoggerFactory.getLogger(SoknadActions.class);

    @Value("${saksoversikt.link.url}")
    private String saksoversiktUrl;

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
        return vedleggService.hentVedlegg(behandlingsId, vedleggId, false);
    }

    @POST
    @Path("/send")
    @SjekkTilgangTilSoknad
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId);

        byte[] kvittering = genererPdf(soknad, "/skjema/kvittering");
        if (soknad.erEttersending()) {
            soknadService.sendSoknad(behandlingsId, kvittering);
        } else {
            byte[] soknadPdf;
            if (soknad.erGjenopptak()) {
                soknadPdf = genererPdf(soknad, "/skjema/gjenopptak");
            } else {
                soknadPdf = genererPdf(soknad, "/skjema/dagpenger");
            }
            vedleggService.lagreKvitteringSomVedlegg(behandlingsId, kvittering);
            soknadService.sendSoknad(behandlingsId, soknadPdf);
        }
    }

    //TODO: trenger man å wrappe epost i eget objekt?
    @POST
    @Path("/fortsettsenere")
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, FortsettSenere epost, @Context HttpServletRequest request) {
        String content = messageSource.getMessage("fortsettSenere.sendEpost.epostInnhold",
                new Object[]{getGjenopptaUrl(request.getRequestURL().toString(), behandlingsId)}, new Locale("nb", "NO"));
        emailService.sendFortsettSenereEPost(epost.getEpost(), "Lenke til påbegynt dagpengesøknad", content);
    }

    @POST
    @Path("/bekreftinnsending")
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, SoknadBekreftelse soknadBekreftelse, @Context HttpServletRequest request) {
        if (soknadBekreftelse.getEpost() != null) {
            String subject = messageSource.getMessage("sendtSoknad.sendEpost.epostSubject", null, new Locale("nb", "NO"));
            String ettersendelseUrl = getEttersendelseUrl(request.getRequestURL().toString(), behandlingsId);
            String saksoversiktLink = saksoversiktUrl + "/detaljer/" + soknadBekreftelse.getTemaKode() + "/" + behandlingsId;
            String innhold = messageSource.getMessage("sendtSoknad.sendEpost.epostInnhold", new Object[]{saksoversiktLink, ettersendelseUrl}, new Locale("nb", "NO"));
            if (soknadBekreftelse.getErEttersendelse()) {
                innhold = messageSource.getMessage("sendEttersendelse.sendEpost.epostInnhold", new Object[]{saksoversiktLink}, new Locale("nb", "NO"));
            }

            // getMessage stripper vekk ytterske lag med p-tags. Siden vi i eposten ønskelig mulighet for flere
            // paragrader må man legge på igjen p-tagsene for å unngå potsensielle feil i HTMLen
            innhold = "<p>" + innhold + "</p>";

            emailService.sendEpostEtterInnsendtSoknad(soknadBekreftelse.getEpost(), subject, innhold, behandlingsId);

        } else {
            logger.debug("Fant ingen epost, sender ikke mail for innsendig");
        }
    }

    private byte[] genererPdf(WebSoknad soknad, String hbsSkjemaPath) {
        String pdfMarkup;
        try {
            vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup av søknad", e);
        }

        return PDFFabrikk.lagPdfFil(pdfMarkup);
    }
}
