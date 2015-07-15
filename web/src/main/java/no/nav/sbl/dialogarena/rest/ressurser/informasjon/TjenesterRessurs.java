package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/")
@Produces(APPLICATION_JSON)
public class TjenesterRessurs {

    @GET
    @Path("/aktiviteter")
    public List<Faktum> hentAktiviteter() {
        return Arrays.asList(
                new Faktum().medKey("aktivitet").medProperty("fom", "2015-01-15").medProperty("tom", "2015-02-15").medProperty("id", "9999").medProperty("navn", "Arbeidspraksis i ordinær virksomhet"),
                new Faktum().medKey("aktivitet").medProperty("fom", "2015-02-31").medProperty("tom", "").medProperty("id", "8888").medProperty("navn", "Arbeid med bistand"));
    }
}
