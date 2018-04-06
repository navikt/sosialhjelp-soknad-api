package no.nav.sbl.dialogarena.rest.ressurser.eksponerte;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.*;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Eksponerer metadata om brukers søknader for bruk i Saksoversikt
 * Implementerer speccen definert i soeknadsskjemaSosialhjelp-v1-saksoversiktdefinisjon
 */
@Controller
@Path("/metadata")
@Timed
@Produces(APPLICATION_JSON)
public class MetadataRessurs {

    @GET
    @Path("/innsendte")
    public InnsendteSoknaderRespons hentInnsendteSoknaderForBruker() {
        String fnr = SubjectHandler.getSubjectHandler().getUid();

        InnsendtSoknad dummyInnsendt = new InnsendtSoknad().withAvsender(
                new Part()
                        .withVisningsNavn("Deg (fnr " + fnr + ")")
                        .withType(Part.Type.BRUKER)
        );

        return new InnsendteSoknaderRespons()
                .withInnsendteSoknader(asList(dummyInnsendt));
    }

    @GET
    @Path("/ettersendelse")
    public EttersendingerRespons hentSoknaderBrukerKanEttersendePa() {
        String fnr = SubjectHandler.getSubjectHandler().getUid();

        return new EttersendingerRespons()
                .withEttersendingsSoknader(asList(new EttersendingsSoknad()
                .withTittel("fnr var " + fnr)));
    }


    @GET
    @Path("/pabegynte")
    public PabegynteSoknaderRespons hentPabegynteSoknaderForBruker() {
        String fnr = SubjectHandler.getSubjectHandler().getUid();

        return new PabegynteSoknaderRespons()
                .withPabegynteSoknader(asList(new PabegyntSoknad()
                        .withTittel("fnr var " + fnr)));
    }
}
