package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService.Forventning;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sbl.dialogarena.rest.ressurser.EttersendingRessurs.getByteArray;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Vedlegg;


@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/sosialhjelpvedlegg")
@Timed
public class SosialhjelpVedleggRessurs {

    @Inject
    private VedleggService vedleggService;

    @Inject
    private VedleggOriginalFilerService vedleggOriginalFilerService;

    @GET
    @Path("/oppdaterVedlegg/{behandlingsId}")
    @Produces(APPLICATION_JSON)
    @SjekkTilgangTilSoknad
    public WebSoknad oppdaterVedleggFaktum(@PathParam("behandlingsId") String behandlingsId) {
        return vedleggOriginalFilerService.oppdaterVedleggOgBelopFaktum(behandlingsId);
    }

    @POST
    @Path("/originalfil/{faktumId}")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    @SjekkTilgangTilSoknad(type = Type.Faktum)
    public Forventning lastOppOriginalfil(@PathParam("faktumId") final Long faktumId, @FormDataParam("file") final FormDataBodyPart fil) {
        if (fil.getValueAs(File.class).length() > MAKS_TOTAL_FILSTORRELSE) {
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null, "vedlegg.opplasting.feil.forStor");
        }

        String filnavn = fil.getContentDisposition().getFileName();
        byte[] data = getByteArray(fil);
        vedleggOriginalFilerService.validerFil(data);

        Forventning forventning = vedleggOriginalFilerService.lagEllerFinnVedleggsForventning(faktumId);
        vedleggOriginalFilerService.leggTilOriginalVedlegg(forventning.vedlegg, data, filnavn);
        Vedlegg oppdatertVedlegg = vedleggService.hentVedlegg(forventning.vedlegg.getVedleggId(), false);

        return new Forventning(forventning.faktum, oppdatertVedlegg, forventning.nyForventning);
    }

    @DELETE
    @Path("/{vedleggId}")
    @Produces(APPLICATION_JSON)
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public Vedlegg slettOriginalFil(@PathParam("vedleggId") final Long vedleggId) {
        return vedleggOriginalFilerService.slettOriginalVedlegg(vedleggId);
    }

    @GET
    @Path("/{vedleggId}/fil")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public Response hentVedleggData(@PathParam("vedleggId") final Long vedleggId, @Context HttpServletResponse response) {
        Vedlegg vedlegg = vedleggService.hentVedlegg(vedleggId, true);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + vedlegg.lagFilNavn() + "\"");
        return Response.ok(vedlegg.getData()).type(vedlegg.getMimetype()).build();
    }

}
