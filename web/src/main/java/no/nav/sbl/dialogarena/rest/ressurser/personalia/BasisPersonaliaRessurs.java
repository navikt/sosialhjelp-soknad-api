package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
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
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/personalia/basisPersonalia")
@Timed
@Produces(APPLICATION_JSON)
public class BasisPersonaliaRessurs {

    @Inject
    private Adressekodeverk adressekodeverk;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @GET
    public BasisPersonaliaFrontend hentBasisPersonalia(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = subjectHandlerWrapper.getIdent();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();

        return mapToBasisPersonaliaFrontend(soknad.getSoknad().getData().getPersonalia());
    }

    private BasisPersonaliaFrontend mapToBasisPersonaliaFrontend(JsonPersonalia jsonPersonalia) {
        final JsonNavn navn = jsonPersonalia.getNavn();
        return new BasisPersonaliaFrontend()
                .withNavn(new NavnFrontend(navn.getFornavn(), navn.getMellomnavn(), navn.getEtternavn()))
                .withFodselsnummer(jsonPersonalia.getPersonIdentifikator().getVerdi())
                .withStatsborgerskap(jsonPersonalia.getStatsborgerskap() == null ? null :
                        adressekodeverk.getLand(jsonPersonalia.getStatsborgerskap().getVerdi()))
                .withNordiskBorger(jsonPersonalia.getNordiskBorger() != null ? jsonPersonalia.getNordiskBorger().getVerdi() : null);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BasisPersonaliaFrontend {
        public NavnFrontend navn;
        public String fodselsnummer;
        public String statsborgerskap;
        public Boolean nordiskBorger;

        public BasisPersonaliaFrontend withNavn(NavnFrontend navn) {
            this.navn = navn;
            return this;
        }

        public BasisPersonaliaFrontend withFodselsnummer(String fodselsnummer) {
            this.fodselsnummer = fodselsnummer;
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
