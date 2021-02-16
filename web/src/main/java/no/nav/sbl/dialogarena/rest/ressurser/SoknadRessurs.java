package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.utils.NedetidUtils;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService;
import no.nav.sosialhjelp.soknad.business.exceptions.SoknadenHarNedetidException;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.business.pdf.HtmlGenerator;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.generateXsrfToken;
import static no.nav.sbl.dialogarena.utils.NedetidUtils.NEDETID_SLUTT;
import static no.nav.sbl.dialogarena.utils.NedetidUtils.getNedetidAsStringOrNull;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknader")
@Timed
@Produces(APPLICATION_JSON)
public class SoknadRessurs {

    public static final String XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API";

    @Inject
    private SoknadService soknadService;

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SystemdataUpdater systemdata;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private HenvendelseService henvendelseService;

    @GET
    @Path("/{behandlingsId}/xsrfCookie")
    public boolean hentXsrfCookie(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId);
        response.addCookie(xsrfCookie(behandlingsId));
        response.addCookie(xsrfCookieMedBehandlingsid(behandlingsId));
        henvendelseService.oppdaterSistEndretDatoPaaMetadata(behandlingsId);
        return true;
    }

    @GET
    @Path("/{behandlingsId}")
    @Produces("application/vnd.oppsummering+html")
    public String hentOppsummering(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        return pdfTemplate.fyllHtmlMalMedInnhold(soknadUnderArbeid.getJsonInternalSoknad(), false);
    }

    @GET
    @Path("/{behandlingsId}/erSystemdataEndret")
    public boolean sjekkOmSystemdataErEndret(@PathParam("behandlingsId") String behandlingsId, @HeaderParam(value = AUTHORIZATION) String token) {
        final String eier = SubjectHandler.getUserId();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        systemdata.update(soknadUnderArbeid, token);

        final JsonInternalSoknad updatedJsonInternalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        SoknadUnderArbeid notUpdatedSoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        final JsonInternalSoknad notUpdatedJsonInternalSoknad = notUpdatedSoknadUnderArbeid.getJsonInternalSoknad();

        soknadUnderArbeidService.sortOkonomi(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi());
        soknadUnderArbeidService.sortOkonomi(notUpdatedSoknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi());
        soknadUnderArbeidService.sortArbeid(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid());
        soknadUnderArbeidService.sortArbeid(notUpdatedSoknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid());

        if (updatedJsonInternalSoknad.equals(notUpdatedJsonInternalSoknad)) {
            return false;
        } else {
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
            return true;
        }
    }

    @POST
    @Path("/{behandlingsId}/oppdaterSamtykker")
    public void oppdaterSamtykker(@PathParam("behandlingsId") String behandlingsId,
                                  @RequestBody List<BekreftelseRessurs> samtykker,
                                  @HeaderParam(value = AUTHORIZATION) String token) {
        boolean harBostotteSamtykke = samtykker.stream()
                .anyMatch(bekreftelse -> bekreftelse.type.equalsIgnoreCase(BOSTOTTE_SAMTYKKE) && bekreftelse.verdi);
        boolean harSkatteetatenSamtykke = samtykker.stream()
                .anyMatch(bekreftelse -> bekreftelse.type.equalsIgnoreCase(UTBETALING_SKATTEETATEN_SAMTYKKE) && bekreftelse.verdi);
        soknadService.oppdaterSamtykker(behandlingsId, harBostotteSamtykke, harSkatteetatenSamtykke, token);
    }

    @GET
    @Path("/{behandlingsId}/hentSamtykker")
    public List<BekreftelseRessurs> hentSamtykker(@PathParam("behandlingsId") String behandlingsId,
                                                  @HeaderParam(value = AUTHORIZATION) String token) {
        final String eier = SubjectHandler.getUserId();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        List<JsonOkonomibekreftelse> bekreftelser = new ArrayList<>();
        hentBekreftelse(soknadUnderArbeid, BOSTOTTE_SAMTYKKE).ifPresent(bekreftelser::add);
        hentBekreftelse(soknadUnderArbeid, UTBETALING_SKATTEETATEN_SAMTYKKE).ifPresent(bekreftelser::add);

        return bekreftelser.stream()
                .filter(JsonOkonomibekreftelse::getVerdi)
                .map(BekreftelseRessurs::new)
                .collect(Collectors.toList());
    }

    private Optional<JsonOkonomibekreftelse> hentBekreftelse(SoknadUnderArbeid soknadUnderArbeid, String samtykke) {
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad()
                .getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
        return bekreftelser.stream()
                .filter(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(samtykke))
                .findFirst();
    }

    @POST
    @Path("/opprettSoknad")
    @Consumes(APPLICATION_JSON)
    public Map<String, String> opprettSoknad(@QueryParam("ettersendTil") String behandlingsId,
                                             @Context HttpServletResponse response,
                                             @HeaderParam(value = AUTHORIZATION) String token) {
        if (NedetidUtils.isInnenforNedetid()) {
            throw new SoknadenHarNedetidException(String.format("Soknaden har nedetid fram til %s ", getNedetidAsStringOrNull(NEDETID_SLUTT)));
        }

        Map<String, String> result = new HashMap<>();

        String opprettetBehandlingsId;
        if (behandlingsId == null) {
            opprettetBehandlingsId = soknadService.startSoknad(token);
        } else {
            final String eier = SubjectHandler.getUserId();
            Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(behandlingsId, eier);
            if (soknadUnderArbeid.isPresent()) {
                opprettetBehandlingsId = soknadUnderArbeid.get().getBehandlingsId();
            } else {
                opprettetBehandlingsId = soknadService.startEttersending(behandlingsId);
            }
        }
        result.put("brukerBehandlingId", opprettetBehandlingsId);
        response.addCookie(xsrfCookie(opprettetBehandlingsId));
        response.addCookie(xsrfCookieMedBehandlingsid(opprettetBehandlingsId));
        return result;
    }

    @DELETE
    @Path("/{behandlingsId}")
    public void slettSoknad(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        soknadService.avbrytSoknad(behandlingsId);
    }

    private static Cookie xsrfCookie(String behandlingId) {
        Cookie xsrfCookie = new Cookie(XSRF_TOKEN, generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/");
        xsrfCookie.setSecure(true);
        return xsrfCookie;
    }

    private static Cookie xsrfCookieMedBehandlingsid(String behandlingId) {
        Cookie xsrfCookie = new Cookie(XSRF_TOKEN + "-" + behandlingId, generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/");
        xsrfCookie.setSecure(true);
        return xsrfCookie;
    }
}
