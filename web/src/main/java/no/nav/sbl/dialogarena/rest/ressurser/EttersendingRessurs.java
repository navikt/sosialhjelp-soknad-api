package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.BehandlingsKjede;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendelseVedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendelseVedleggService.EttersendelseVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sbl.dialogarena.rest.mappers.VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Metadata;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Vedlegg;


@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/ettersendelse")
@Timed
@Produces(APPLICATION_JSON)
public class EttersendingRessurs {

    @Inject
    private EttersendelseVedleggService ettersendelseVedleggService;

    @Inject
    private InnsendtSoknadService innsendtSoknadService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private SoknadDataFletter soknadDataFletter;

    @Inject
    private SoknadService soknadService;


    @GET
    @Path("/innsendte/{behandlingsId}")
    @SjekkTilgangTilSoknad(type = Metadata)
    public BehandlingsKjede hentBehandlingskjede(@PathParam("behandlingsId") String behandlingsId) {
        return innsendtSoknadService.hentBehandlingskjede(behandlingsId);
    }

    @GET
    @Path("/vedlegg/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public List<EttersendelseVedlegg> legacyHentVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        return ettersendelseVedleggService.hentVedleggForSoknad(behandlingsId);
    }

    @GET
    @Path("/ettersendteVedlegg/{behandlingsId}")
    public List<EttersendtVedlegg> hentVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid;
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (soknadUnderArbeidOptional.isPresent()){
            soknadUnderArbeid = soknadUnderArbeidOptional.get();
        } else {
            WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, true);
            soknadDataFletter.legacyKonverterVedleggOgOppdaterSoknadUnderArbeid(behandlingsId, eier, webSoknad);
            soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        }
        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), eier);
        List<JsonVedlegg> originaleVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();

        return mapVedleggToSortedListOfEttersendteVedlegg(opplastedeVedlegg, originaleVedlegg);
    }

    @POST
    @Path("/vedlegg/{faktumId}")
    @Consumes(MULTIPART_FORM_DATA)
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public List<EttersendelseVedlegg> lastOppVedlegg(@PathParam("faktumId") final Long faktumId, @FormDataParam("file") final FormDataBodyPart fil) {
        if (fil.getValueAs(File.class).length() > MAKS_TOTAL_FILSTORRELSE) {
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null, "vedlegg.opplasting.feil.forStor");
        }

        String filnavn = fil.getContentDisposition().getFileName();
        byte[] data = getByteArray(fil);

        return ettersendelseVedleggService.lastOppVedlegg(faktumId, data, filnavn);
    }

    @DELETE
    @Path("/vedlegg/{vedleggId}")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public List<EttersendelseVedlegg> slettVedlegg(@QueryParam("filId") Long filId, @PathParam("vedleggId") final Long vedleggId) {
        return ettersendelseVedleggService.slettVedlegg(filId);
    }


    public static byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

}
