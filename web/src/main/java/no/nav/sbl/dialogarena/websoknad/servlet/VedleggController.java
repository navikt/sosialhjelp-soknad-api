package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

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
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * Controller klasse som brukes til å laste opp filer fra frontend.
 */
@Controller()
@Path("/vedlegg/{vedleggId}")
@Produces(APPLICATION_JSON)
public class VedleggController {

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SendSoknadService soknadService;

    private static final Integer MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10;

    @GET
    @SjekkTilgangTilSoknad
    public Vedlegg hentVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId) {
        return vedleggService.hentVedlegg(soknadId, vedleggId, false);
    }

    //TODO: bør kanskje returnere det oppdaterte vedlegget
    @PUT
    @SjekkTilgangTilSoknad
    public void lagreVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId, @RequestBody Vedlegg vedlegg) {
        vedleggService.lagreVedlegg(soknadId, vedleggId, vedlegg);
    }

    //TODO: denne sletter kun vedlegg som er 'underbehandling', hvis ikke resettes den tilbake til en vedleggsforventning
    @DELETE
    @SjekkTilgangTilSoknad
    public void slettVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId) {
        vedleggService.slettVedlegg(soknadId, vedleggId);
    }

    @GET
    @Path("/fil")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentVedleggUnderBehandling(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId) {
        Vedlegg forventning = vedleggService.hentVedlegg(soknadId, vedleggId, false);
        return vedleggService.hentVedleggUnderBehandling(soknadId, forventning.getFillagerReferanse());
    }

    @GET
    @Path("/fil")
    @Produces(APPLICATION_OCTET_STREAM)
    @SjekkTilgangTilSoknad
    public byte[] hentVedleggData(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId, @Context HttpServletResponse response) {
        Vedlegg vedlegg = vedleggService.hentVedlegg(soknadId, vedleggId, true);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + vedlegg.getVedleggId() + ".pdf\"");
        return vedlegg.getData();
    }

    @GET
    @Path("/fil")
    @Produces(IMAGE_PNG_VALUE)
    @SjekkTilgangTilSoknad
    public byte[] lagForhandsvisningForVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId, @QueryParam("side") final int side) {
        return vedleggService.lagForhandsvisning(soknadId, vedleggId, side);
    }

    @POST
    @Path("/fil")
    @Consumes(MULTIPART_FORM_DATA)
    @SjekkTilgangTilSoknad(sjekkXsrf = false)
    public List<Vedlegg> lastOppFiler(@PathParam("vedleggId") final Long vedleggId, @QueryParam("soknadId") final Long soknadId,
                                          @QueryParam("X-XSRF-TOKEN") final String xsrfToken, @FormDataParam("files") final List<FormDataBodyPart> files) {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        String brukerBehandlingId = soknad.getBrukerBehandlingId();
        if (soknad.getBehandlingskjedeId() != null) {
            brukerBehandlingId = soknad.getBehandlingskjedeId();
        }

        XsrfGenerator.sjekkXsrfToken(xsrfToken, brukerBehandlingId);
        Vedlegg forventning = vedleggService.hentVedlegg(soknadId, vedleggId, false);

        if (erFilForStor(soknadId, files, forventning)) {
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null,
                    "vedlegg.opplasting.feil.forStor");
        }

        List<Vedlegg> res = new ArrayList<>();
        for (FormDataBodyPart file : files) {
            byte[] in = getByteArray(file);
            Vedlegg vedlegg = new Vedlegg()
                    .medVedleggId(null)
                    .medSoknadId(soknadId)
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
                res.add(vedleggService.hentVedlegg(soknadId, id, false));
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

    private Boolean erFilForStor(Long soknadId, List<FormDataBodyPart> files, Vedlegg forventning) {
        Long totalStorrelse = 0L;
        List<Vedlegg> alleVedlegg = vedleggService.hentVedleggUnderBehandling(soknadId, forventning.getFillagerReferanse());
        for (Vedlegg vedlegg : alleVedlegg) {
            totalStorrelse += vedlegg.getStorrelse();
        }

        for (FormDataBodyPart file : files) {
            totalStorrelse += file.getContentDisposition().getSize();
        }

        return totalStorrelse > MAKS_TOTAL_FILSTORRELSE;
    }

}
