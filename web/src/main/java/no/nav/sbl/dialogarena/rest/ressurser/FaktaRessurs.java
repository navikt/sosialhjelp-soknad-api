package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import org.apache.commons.collections15.Predicate;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.modig.lang.collections.IterUtils.on;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Path("/fakta")
@Produces(APPLICATION_JSON)
public class FaktaRessurs {

    @Inject
    private FaktaService faktaService;

    @Inject
    private VedleggService vedleggService;

    //todo: legg til GET for faktum

    @POST
    @Consumes(APPLICATION_JSON)
    @SjekkTilgangTilSoknad
    public Faktum opprettFaktum(@QueryParam("soknadId") final Long soknadId, Faktum faktum) {
        return faktaService.lagreSoknadsFelt(soknadId, faktum);
    }

    @PUT
    @Path("/{faktumId}")
    @Consumes(APPLICATION_JSON)
    @SjekkTilgangTilSoknad
    public Faktum lagreFaktum(@PathParam("faktumId") final Long faktumId, Faktum faktum) {
        return faktaService.lagreSoknadsFelt(faktum.getSoknadId(), faktum);
    }

    // TODO: Fjern soknadId
    @DELETE
    @Path("/{faktumId}")
    @SjekkTilgangTilSoknad
    public void slettFaktum(@PathParam("faktumId") final Long faktumId, @QueryParam("soknadId") final Long soknadId) {
        faktaService.slettBrukerFaktum(soknadId, faktumId);
    }

    // TODO: Fjern soknadId
    @GET
    @Path("/{faktumId}/vedlegg")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentVedlegg(@PathParam("faktumId") final Long faktumId, @QueryParam("soknadId") final Long soknadId) {
        return on(vedleggService.hentPaakrevdeVedlegg(soknadId)).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedleggForventning) {
                return vedleggForventning.getFaktumId().equals(faktumId);
            }
        }).collect();
    }

}
