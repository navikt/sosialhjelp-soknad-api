package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/personalia/basisPersonalia")
@Timed
@Produces(APPLICATION_JSON)
public class BasisPersonaliaRessurs {

    @Inject
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @GET
    public BasisPersonaliaFrontend hentBasisPersonalia(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        JsonPersonalia jsonPersonalia = basisPersonaliaSystemdata.innhentSystemBasisPersonalia(eier);

        return mapToBasisPersonaliaFrontend(jsonPersonalia);
    }

    private BasisPersonaliaFrontend mapToBasisPersonaliaFrontend(JsonPersonalia jsonPersonalia) {
        return new BasisPersonaliaFrontend()
                .withPersonIdentifikator(jsonPersonalia.getPersonIdentifikator().getVerdi())
                .withFornavn(jsonPersonalia.getNavn().getFornavn())
                .withMellomnavn(jsonPersonalia.getNavn().getMellomnavn())
                .withEtternavn(jsonPersonalia.getNavn().getEtternavn())
                .withFulltNavn(toFulltNavn(jsonPersonalia.getNavn()))
                .withStatsborgerskap(jsonPersonalia.getStatsborgerskap() != null ? jsonPersonalia.getStatsborgerskap().getVerdi() : null)
                .withNordiskBorger(jsonPersonalia.getNordiskBorger() != null ? jsonPersonalia.getNordiskBorger().getVerdi() : null);
    }

    private String toFulltNavn(JsonSokernavn jsonSokernavn){
        final String f = jsonSokernavn.getFornavn() != null ? jsonSokernavn.getFornavn() : "";
        final String m = jsonSokernavn.getMellomnavn() != null ? " " + jsonSokernavn.getMellomnavn() : "";
        final String e = jsonSokernavn.getEtternavn() != null ? " " + jsonSokernavn.getEtternavn() : "";
        return f + m + e;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BasisPersonaliaFrontend {
        public String personIdentifikator;
        public String fornavn;
        public String mellomnavn;
        public String etternavn;
        public String fulltNavn;
        public String statsborgerskap;
        public Boolean nordiskBorger;

        public BasisPersonaliaFrontend withPersonIdentifikator(String personIdentifikator) {
            this.personIdentifikator = personIdentifikator;
            return this;
        }

        public BasisPersonaliaFrontend withFornavn(String fornavn) {
            this.fornavn = fornavn;
            return this;
        }

        public BasisPersonaliaFrontend withMellomnavn(String mellomnavn) {
            this.mellomnavn = mellomnavn;
            return this;
        }

        public BasisPersonaliaFrontend withEtternavn(String etternavn) {
            this.etternavn = etternavn;
            return this;
        }

        public BasisPersonaliaFrontend withFulltNavn(String fulltNavn) {
            this.fulltNavn = fulltNavn;
            return this;
        }

        public BasisPersonaliaFrontend withStatsborgerskap(String statsborgerskap) {
            this.statsborgerskap = statsborgerskap;
            return this;
        }

        public BasisPersonaliaFrontend withNordiskBorger(Boolean nordiskBorger){
            this.nordiskBorger = nordiskBorger;
            return this;
        }
    }
}
