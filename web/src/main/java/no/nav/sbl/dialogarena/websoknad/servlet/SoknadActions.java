package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/soknad/{soknadId}/actions")
@Produces(APPLICATION_JSON)
public class SoknadActions {

    @Inject
    private VedleggService vedleggService;

    @GET
    @Path("/leggved")
    @SjekkTilgangTilSoknad
    public Vedlegg leggVedVedlegg(@PathParam("soknadId") final Long soknadId, @QueryParam("vedleggId") final Long vedleggId) {
        vedleggService.genererVedleggFaktum(soknadId, vedleggId);
        return vedleggService.hentVedlegg(soknadId, vedleggId, false);
    }
}
