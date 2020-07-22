package no.nav.sbl.dialogarena.rest.ressurser.vedlegg;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.ressurser.FilFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.OpplastetVedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.FileDetectionUtils;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sbl.dialogarena.rest.ressurser.EttersendingRessurs.getByteArray;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/opplastetVedlegg")
@Produces(APPLICATION_JSON)
@Timed
public class OpplastetVedleggRessurs {

    public static final Integer MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private OpplastetVedleggService opplastetVedleggService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @GET
    @Path("/{vedleggId}")
    @Produces(APPLICATION_JSON)
    public OpplastetVedlegg getVedlegg(@PathParam("vedleggId") final String vedleggId) {
        final String eier = subjectHandlerWrapper.getIdent();
        return opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);
    }

    @GET
    @Path("/{vedleggId}/fil")
    @Produces(APPLICATION_JSON)
    public Response getVedleggFil(@PathParam("vedleggId") final String vedleggId, @Context HttpServletResponse response) {
        final String eier = subjectHandlerWrapper.getIdent();
        OpplastetVedlegg opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);
        if (opplastetVedlegg != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + opplastetVedlegg.getFilnavn() + "\"");
        } else {
            return Response.noContent().build();
        }
        String mimetype = FileDetectionUtils.getMimeType(opplastetVedlegg.getData());
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

        if (!MockUtils.isTillatMockRessurs()) {
            // Oracle-spesifikk syntax i query: disabler da denne sjekken på mock (hvor hsqldb brukes)
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
}
