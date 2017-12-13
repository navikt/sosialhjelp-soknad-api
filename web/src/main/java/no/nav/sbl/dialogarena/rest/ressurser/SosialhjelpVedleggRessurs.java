package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.sort;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Vedlegg;


@Controller
@Path("/sosialhjelpvedlegg")
@Produces(APPLICATION_JSON)
@Timed
public class SosialhjelpVedleggRessurs {

    @Inject
    private VedleggService vedleggService;

    @Inject
    private VedleggOriginalFilerService vedleggOriginalFilerService;

    @Inject
    private SoknadService soknadService;


    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @GET
    @Path("/oppdaterVedlegg/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public WebSoknad oppdaterVedleggFaktum(@PathParam("behandlingsId") String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);

        List<FaktumStruktur> faktaStruktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer()).getFakta();
        sort(faktaStruktur, sammenlignEtterDependOn());

        List<FaktumStruktur> opplysninger = faktaStruktur.stream()
                .filter(struktur -> struktur.getId().startsWith("opplysninger."))
                .filter(struktur -> soknad.getFaktaMedKey(struktur.getId()).isEmpty())
                .collect(Collectors.toList());

        Iterator<Long> faktumIder = lokalDb.hentLedigeFaktumIder(opplysninger.size()).iterator();

        List<Faktum> nyeFakta = new ArrayList<>();

        for (FaktumStruktur struktur : opplysninger) {
            Long parentFaktumId = null;

            if (struktur.getDependOn() != null) {
                if (soknad.getFaktumMedKey(struktur.getDependOn().getId()) != null) {
                    parentFaktumId = soknad.getFaktumMedKey(struktur.getDependOn().getId()).getFaktumId();
                } else {
                    parentFaktumId = nyeFakta.stream()
                            .filter(f -> f.getKey().equals(struktur.getDependOn().getId()))
                            .findFirst()
                            .map(Faktum::getFaktumId)
                            .orElse(null);
                }
            }

            nyeFakta.add(new Faktum()
                    .medFaktumId(faktumIder.next())
                    .medParrentFaktumId(parentFaktumId)
                    .medKey(struktur.getId())
                    .medType(BRUKERREGISTRERT)
                    .medSoknadId(soknad.getSoknadId()));
        }

        lokalDb.batchOpprettTommeFakta(nyeFakta);

        WebSoknad soknadOppdatert = soknadService.hentSoknad(behandlingsId, true, true);
        return soknadOppdatert;
    }

    @POST
    @Path("/originalfil/{faktumId}")
    @Consumes(MULTIPART_FORM_DATA)
    @SjekkTilgangTilSoknad(type = Type.Faktum)
    public Vedlegg lastOppOriginalfil(@PathParam("faktumId") final Long faktumId, @FormDataParam("file") final FormDataBodyPart fil) {
        if (fil.getValueAs(File.class).length() > MAKS_TOTAL_FILSTORRELSE) {
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null, "vedlegg.opplasting.feil.forStor");
        }

        String filnavn = fil.getContentDisposition().getFileName();
        byte[] data = getByteArray(fil);

        Vedlegg vedlegg = vedleggOriginalFilerService.lagEllerFinnVedleggsForventning(faktumId);
        vedleggOriginalFilerService.leggTilOriginalVedlegg(vedlegg, data, filnavn);
        return vedleggService.hentVedlegg(vedlegg.getVedleggId(), false);
    }

    @DELETE
    @Path("/{vedleggId}")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public void slettOriginalFil(@PathParam("vedleggId") final Long vedleggId) {
        vedleggOriginalFilerService.slettOriginalVedlegg(vedleggId);
    }

    @GET
    @Path("/{vedleggId}/fil")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public byte[] hentVedleggData(@PathParam("vedleggId") final Long vedleggId, @Context HttpServletResponse response) {
        Vedlegg vedlegg = vedleggService.hentVedlegg(vedleggId, true);
        response.setContentType(vedlegg.getMimetype());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + vedlegg.lagFilNavn() + "\"");
        return vedlegg.getData();
    }

    private static byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

}
