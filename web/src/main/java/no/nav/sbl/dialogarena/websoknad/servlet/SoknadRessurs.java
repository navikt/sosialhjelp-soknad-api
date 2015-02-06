package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/soknad")
@Produces(APPLICATION_JSON)
public class SoknadRessurs {

    @Inject
    private FaktaService faktaService;

    @Inject
    private VedleggService vedleggService;

    @GET
    @SjekkTilgangTilSoknad
    public List<Faktum> hentSoknadData(@PathParam("soknadId") final Long soknadId) {
        return faktaService.hentFakta(soknadId);
    }

    @GET
    @Path("/{soknadId}/vedlegg")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedlegg(@PathParam("soknadId") final Long soknadId) {
        return vedleggService.hentPaakrevdeVedlegg(soknadId);
    }

}
