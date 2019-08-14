package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.BehandlingsKjede;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg;


@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/ettersendelse")
@Timed
@Produces(APPLICATION_JSON)
public class EttersendingRessurs {

    @Inject
    private InnsendtSoknadService innsendtSoknadService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @GET
    @Path("/innsendte/{behandlingsId}")
    public BehandlingsKjede hentBehandlingskjede(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        return innsendtSoknadService.hentBehandlingskjede(behandlingsId, eier);
    }

    @GET
    @Path("/ettersendteVedlegg/{behandlingsId}")
    public List<EttersendtVedlegg> hentVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), eier);
        List<JsonVedlegg> originaleVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();

        return mapVedleggToSortedListOfEttersendteVedlegg(opplastedeVedlegg, originaleVedlegg);
    }


    public static byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

}
