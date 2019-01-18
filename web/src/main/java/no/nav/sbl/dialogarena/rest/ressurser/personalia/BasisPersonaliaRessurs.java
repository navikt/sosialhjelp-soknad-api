package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata.BasisPersonalia;
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
        BasisPersonalia basisPersonalia = basisPersonaliaSystemdata.innhentSystemBasisPersonalia(eier);

        return mapToBasisPersonaliaFrontend(basisPersonalia);
    }

    private BasisPersonaliaFrontend mapToBasisPersonaliaFrontend(BasisPersonalia basisPersonalia) {
        return new BasisPersonaliaFrontend()
                .withPersonIdentifikator(basisPersonalia.personIdentifikator)
                .withNavn(toSammensattNavn(basisPersonalia))
                .withStatsborgerskap(basisPersonalia.statsborgerskap)
                .withNordiskBorger(basisPersonalia.nordiskBorger);
    }

    private String toSammensattNavn(BasisPersonalia base){
        final String f = base.fornavn != null ? base.fornavn : "";
        final String m = base.mellomnavn != null ? " " + base.mellomnavn : "";
        final String e = base.etternavn != null ? " " + base.etternavn : "";
        return f + m + e;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BasisPersonaliaFrontend {
        public String personIdentifikator;
        public String navn;
        public String statsborgerskap;
        public Boolean nordiskBorger;

        public BasisPersonaliaFrontend withPersonIdentifikator(String personIdentifikator) {
            this.personIdentifikator = personIdentifikator;
            return this;
        }

        public BasisPersonaliaFrontend withNavn(String navn) {
            this.navn = navn;
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
