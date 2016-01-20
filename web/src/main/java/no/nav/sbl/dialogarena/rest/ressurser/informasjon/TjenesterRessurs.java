package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

@Controller
@Produces(APPLICATION_JSON)
public class TjenesterRessurs {

    @Inject
    private AktivitetService aktivitetService;

    @Inject
    private MaalgrupperService maalgrupperService;

    @GET
    @Path("/aktiviteter")
    public List<Faktum> hentAktiviteter() {
        return aktivitetService.hentAktiviteter(getSubjectHandler().getUid());
    }

    @GET
    @Path("/vedtak")
    public List<Faktum> hentVedtak() {
        return aktivitetService.hentVedtak(getSubjectHandler().getUid());
    }

    @GET
    @Path("/maalgrupper")
    public List<Faktum> hentMaalgrupper() {
        return maalgrupperService.hentMaalgrupper(getSubjectHandler().getUid());
    }
}
