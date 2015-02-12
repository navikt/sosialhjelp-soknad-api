package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.SoknadStrukturUtils;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.domain.StartSoknad;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

@Path("/soknader")
@Produces(APPLICATION_JSON)
public class SoknadRessurs {

    @Inject
    private FaktaService faktaService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private EttersendingService ettersendingService;

    @Inject
    private HtmlGenerator pdfTemplate;

    @GET
    @Path("/{soknadId}")
    @SjekkTilgangTilSoknad
    public WebSoknad hentSoknadData(@PathParam("soknadId") Long soknadId) {
        return soknadService.hentSoknad(soknadId);
    }

    @GET
    @Path("/{soknadId}")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String hentOppsummering(@PathParam("soknadId") Long soknadId) throws IOException {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
        if (soknad.erGjenopptak()) {
            return pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/gjenopptak");
        } else {
            return pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");
        }
    }


    // TODO: kan vi ta inn request param direkte istedet for StartSoknad + refactor + hva er returverdien
    @POST
    @Consumes(APPLICATION_JSON)
    public Map<String, String> opprettSoknad(@QueryParam("ettersendTil") String behandlingsId, StartSoknad soknadType, @Context HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();
        String fodselsnummer = getSubjectHandler().getUid();

        if (behandlingsId == null) {
            String behandlingId = soknadService.startSoknad(soknadType.getSoknadType(), fodselsnummer);
            result.put("brukerbehandlingId", behandlingId);
        } else {
            WebSoknad soknad = ettersendingService.hentEttersendingForBehandlingskjedeId(behandlingsId);
            Long soknadId;
            if (soknad == null) {
                soknadId = ettersendingService.startEttersending(behandlingsId, fodselsnummer);
            } else {
                soknadId = soknad.getSoknadId();
            }
            result.put("soknadId", soknadId.toString());
        }
        response.addCookie(xsrfCookie(behandlingsId));
        return result;
    }

    @PUT
    @Path("/{soknadId}")
    @SjekkTilgangTilSoknad
    public void settDelstegStatus(@PathParam("soknadId") Long soknadId, @QueryParam("delsteg") String delsteg) {
        if (delsteg == null) {
            throw new ApplicationException("Ugyldig delsteg sendt inn til REST-controller.");
        } else {
            DelstegStatus delstegstatus;
            if (delsteg.equalsIgnoreCase("utfylling")) {
                delstegstatus = DelstegStatus.UTFYLLING;

            } else if (delsteg.equalsIgnoreCase("vedlegg")) {
                delstegstatus = DelstegStatus.SKJEMA_VALIDERT;

            } else if (delsteg.equalsIgnoreCase("oppsummering")) {
                delstegstatus = DelstegStatus.VEDLEGG_VALIDERT;
            } else {
                throw new ApplicationException("Ugyldig delsteg sendt inn til REST-controller.");
            }
            soknadService.settDelsteg(soknadId, delstegstatus);
        }
    }

    @DELETE
    @Path("/{soknadId}")
    @SjekkTilgangTilSoknad
    public void slettSoknad(@PathParam("soknadId") Long soknadId) {
        soknadService.avbrytSoknad(soknadId);
    }

    @GET
    @Path("/{soknadId}/struktur")
    @SjekkTilgangTilSoknad
    public SoknadStruktur hentSoknadStruktur(@PathParam("soknadId") Long soknadId) {
        String skjemanavn = soknadService.hentSoknad(soknadId).getskjemaNummer();
        return SoknadStrukturUtils.hentStruktur(skjemanavn);
    }

    @GET
    @Path("/{soknadId}/fakta")
    @SjekkTilgangTilSoknad
    public List<Faktum> hentFakta(@PathParam("soknadId") final Long soknadId) {
        return faktaService.hentFakta(soknadId);
    }

    @GET
    @Path("/{soknadId}/vedlegg")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedlegg(@PathParam("soknadId") final Long soknadId) {
        return vedleggService.hentPaakrevdeVedlegg(soknadId);
    }

    private static Cookie xsrfCookie(String behandlingId) {
        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/sendsoknad");
        return xsrfCookie;
    }

}
