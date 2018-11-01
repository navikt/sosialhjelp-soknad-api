package no.nav.sbl.dialogarena.rest.ressurser.mock;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/internal/mock/tjeneste")
@Produces(APPLICATION_JSON)
public class TjenesteMockRessurs {

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon/{telefon}")
    public void settTelefon(@PathParam("telefon") String telefon) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        DkifMock.setTelefonnummer(telefon);
    }

    private boolean isTillatMockRessurs() {
        return Boolean.parseBoolean(System.getProperty("tillatMockRessurs", "false"));
    }

}