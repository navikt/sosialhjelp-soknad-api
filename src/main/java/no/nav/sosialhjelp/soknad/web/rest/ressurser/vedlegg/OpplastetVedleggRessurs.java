package no.nav.sosialhjelp.soknad.web.rest.ressurser.vedlegg;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.service.OpplastetVedleggService;
import no.nav.sosialhjelp.soknad.business.util.FileDetectionUtils;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.FilFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/opplastetVedlegg")
@Produces(APPLICATION_JSON)
@Timed
public class OpplastetVedleggRessurs {

    public static final Integer MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10;

    private final OpplastetVedleggRepository opplastetVedleggRepository;
    private final OpplastetVedleggService opplastetVedleggService;
    private final Tilgangskontroll tilgangskontroll;

    public OpplastetVedleggRessurs(OpplastetVedleggRepository opplastetVedleggRepository, OpplastetVedleggService opplastetVedleggService, Tilgangskontroll tilgangskontroll) {
        this.opplastetVedleggRepository = opplastetVedleggRepository;
        this.opplastetVedleggService = opplastetVedleggService;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GET
    @Path("/{vedleggId}")
    @Produces(APPLICATION_JSON)
    public OpplastetVedlegg getVedlegg(@PathParam("vedleggId") final String vedleggId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        final String eier = SubjectHandler.getUserId();
        return opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);
    }

    @GET
    @Path("/{vedleggId}/fil")
    @Produces(APPLICATION_JSON)
    public Response getVedleggFil(@PathParam("vedleggId") final String vedleggId, @Context HttpServletResponse response) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        final String eier = SubjectHandler.getUserId();
        OpplastetVedlegg opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);
        if (opplastetVedlegg != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + opplastetVedlegg.getFilnavn() + "\"");
        } else {
            return Response.noContent().build();
        }
        String mimetype = FileDetectionUtils.getMimeTypeForSending(opplastetVedlegg.getData());
        return Response.ok(opplastetVedlegg.getData()).type(mimetype).build();
    }

    @POST
    @Path("/{behandlingsId}/{type}")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public FilFrontend saveVedlegg(@PathParam("behandlingsId") String behandlingsId, @PathParam("type") String vedleggstype, @FormDataParam("file") final FormDataBodyPart fil) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        if (fil.getValueAs(File.class).length() > MAKS_TOTAL_FILSTORRELSE) {
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null, "vedlegg.opplasting.feil.forStor");
        }

        final String filnavn = fil.getContentDisposition().getFileName();
        final byte[] data = getByteArray(fil);

        if (!MockUtils.isRunningWithInMemoryDb()) {
            // Oracle-spesifikk syntax i query: disabler da denne sjekken når in-memory hsqldb brukes
            opplastetVedleggService.sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId, data);
        }

        final OpplastetVedlegg opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(behandlingsId, vedleggstype, data, filnavn);
        return new FilFrontend().withFilNavn(opplastetVedlegg.getFilnavn()).withUuid(opplastetVedlegg.getUuid());
    }

    @DELETE
    @Path("/{behandlingsId}/{vedleggId}")
    public void deleteVedlegg(@PathParam("behandlingsId") String behandlingsId, @PathParam("vedleggId") final String vedleggId) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId);
    }

    private static byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }
}
