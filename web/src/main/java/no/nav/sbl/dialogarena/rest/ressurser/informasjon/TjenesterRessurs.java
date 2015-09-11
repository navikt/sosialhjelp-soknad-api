package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AktiviteterService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MaalgrupperService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

@Controller
@Path("/")
@Produces(APPLICATION_JSON)
public class TjenesterRessurs {

    @Inject
    private AktiviteterService aktiviteterService;

    @Inject
    private MaalgrupperService maalgrupperService;

    @GET
    @Path("/aktiviteter")
    public List<Faktum> hentAktiviteter() {
        return aktiviteterService.hentAktiviteter(getSubjectHandler().getUid());
    }

    @GET
    @Path("/maalgrupper")
    public List<Faktum> hentMaalgrupper() {
        return maalgrupperService.hentMaalgrupper(getSubjectHandler().getUid());
    }
}
