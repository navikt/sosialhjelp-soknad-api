package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

@Path("/vedlegg/{vedleggId}")
@Produces(APPLICATION_JSON)
public class VedleggRessurs {

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SendSoknadService soknadService;

    private static final Integer MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10;

    @GET
    @SjekkTilgangTilSoknad
    public Vedlegg hentVedlegg(@PathParam("vedleggId") final Long vedleggId) {
        return vedleggService.hentVedlegg(vedleggId, false);
    }

    @PUT
    @SjekkTilgangTilSoknad
    public void lagreVedlegg(@PathParam("vedleggId") final Long vedleggId, Vedlegg vedlegg) {
        vedleggService.lagreVedlegg(vedleggId, vedlegg);
    }

    @DELETE
    @SjekkTilgangTilSoknad
    public void slettVedlegg(@PathParam("vedleggId") final Long vedleggId) {
        vedleggService.slettVedlegg(vedleggId);
    }

    @GET
    @Path("/fil")
    @Produces(APPLICATION_JSON)
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentVedleggUnderBehandling(@PathParam("vedleggId") final Long vedleggId, @QueryParam("behandlingsId") final String behandlingsId) {
        Vedlegg forventning = vedleggService.hentVedlegg(vedleggId, false);
        return vedleggService.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
    }

    @GET
    @Path("/fil")
    @Produces(APPLICATION_OCTET_STREAM)
    @SjekkTilgangTilSoknad
    public byte[] hentVedleggData(@PathParam("vedleggId") final Long vedleggId, @Context HttpServletResponse response) {
        Vedlegg vedlegg = vedleggService.hentVedlegg(vedleggId, true);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + vedlegg.getVedleggId() + ".pdf\"");
        return vedlegg.getData();
    }

    @GET
    @Path("/fil")
    @Produces("image/*")
    @SjekkTilgangTilSoknad
    public byte[] lagForhandsvisningForVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("side") final int side) {
        return vedleggService.lagForhandsvisning(vedleggId, side);
    }

    @POST
    @Path("/fil")
    @Consumes(MULTIPART_FORM_DATA)
    @SjekkTilgangTilSoknad
    public List<Vedlegg> lastOppFiler(@PathParam("vedleggId") final Long vedleggId, @QueryParam("behandlingsId") String behandlingsId,
                                        @FormDataParam("files[]") final List<FormDataBodyPart> files) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId);
        if (soknad.getBehandlingskjedeId() != null) {
            behandlingsId = soknad.getBehandlingskjedeId();
        }

        Vedlegg forventning = vedleggService.hentVedlegg(vedleggId, false);

        if (erFilForStor(behandlingsId, files, forventning)) {
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null,
                    "vedlegg.opplasting.feil.forStor");
        }

        List<Vedlegg> res = new ArrayList<>();
        for (FormDataBodyPart file : files) {
            byte[] in = getByteArray(file);
            Vedlegg vedlegg = new Vedlegg()
                    .medVedleggId(null)
                    .medSoknadId(soknad.getSoknadId())
                    .medFaktumId(forventning.getFaktumId())
                    .medSkjemaNummer(forventning.getSkjemaNummer())
                    .medNavn(forventning.getNavn())
                    .medStorrelse(file.getContentDisposition().getSize())
                    .medAntallSider(1)
                    .medFillagerReferanse(forventning.getFillagerReferanse())
                    .medData(in)
                    .medOpprettetDato(forventning.getOpprettetDato())
                    .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);

            List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, new ByteArrayInputStream(in));
            for (Long id : ids) {
                res.add(vedleggService.hentVedlegg(id, false));
            }
        }
        return res;
    }

    private static byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

    private Boolean erFilForStor(String behandlingsId, List<FormDataBodyPart> files, Vedlegg forventning) {
        Long totalStorrelse = 0L;
        List<Vedlegg> alleVedlegg = vedleggService.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
        for (Vedlegg vedlegg : alleVedlegg) {
            totalStorrelse += vedlegg.getStorrelse();
        }

        for (FormDataBodyPart file : files) {
            totalStorrelse += file.getContentDisposition().getSize();
        }

        return totalStorrelse > MAKS_TOTAL_FILSTORRELSE;
    }

}
