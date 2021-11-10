package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.systemdata.AdresseSystemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs;
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend;
import no.nav.sosialhjelp.soknad.web.rest.mappers.AdresseMapper;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/personalia/adresser")
@Timed
@Produces(APPLICATION_JSON)
public class AdresseRessurs {

    private final Tilgangskontroll tilgangskontroll;
    private final AdresseSystemdata adresseSystemdata;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final NavEnhetRessurs navEnhetRessurs;

    public AdresseRessurs(
            Tilgangskontroll tilgangskontroll,
            AdresseSystemdata adresseSystemdata,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            NavEnhetRessurs navEnhetRessurs
    ) {
        this.tilgangskontroll = tilgangskontroll;
        this.adresseSystemdata = adresseSystemdata;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.navEnhetRessurs = navEnhetRessurs;
    }

    @GET
    public AdresserFrontend hentAdresser(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        String personIdentifikator = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        JsonAdresse jsonOppholdsadresse = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();

        JsonAdresse sysFolkeregistrertAdresse = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        JsonAdresse sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(personIdentifikator);

        soknad.getJsonInternalSoknad().setMidlertidigAdresse(sysMidlertidigAdresse);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);

        return AdresseMapper.mapToAdresserFrontend(sysFolkeregistrertAdresse, sysMidlertidigAdresse, jsonOppholdsadresse);
    }

    @PUT
    public List<NavEnhetFrontend> updateAdresse(@PathParam("behandlingsId") String behandlingsId, AdresserFrontend adresserFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();

        switch (adresserFrontend.valg){
            case FOLKEREGISTRERT:
                personalia.setOppholdsadresse(adresseSystemdata.createDeepCopyOfJsonAdresse(personalia.getFolkeregistrertAdresse()));
                break;
            case MIDLERTIDIG:
                personalia.setOppholdsadresse(adresseSystemdata.innhentMidlertidigAdresse(eier));
                break;
            case SOKNAD:
                personalia.setOppholdsadresse(AdresseMapper.mapToJsonAdresse(adresserFrontend.soknad));
                break;
        }

        personalia.getOppholdsadresse().setAdresseValg(adresserFrontend.valg);
        personalia.setPostadresse(midlertidigLosningForPostadresse(personalia.getOppholdsadresse()));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
        return navEnhetRessurs.findSoknadsmottaker(eier, soknad.getJsonInternalSoknad().getSoknad(), adresserFrontend.valg.toString(), null);
    }

    private JsonAdresse midlertidigLosningForPostadresse(JsonAdresse oppholdsadresse) {
        if (oppholdsadresse == null){
            return null;
        }
        if (oppholdsadresse.getType() == JsonAdresse.Type.MATRIKKELADRESSE){
            return null;
        }
        return adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse).withAdresseValg(null);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AdresserFrontend {
        public JsonAdresseValg valg;
        public AdresseFrontend folkeregistrert;
        public AdresseFrontend midlertidig;
        public AdresseFrontend soknad;

        public AdresserFrontend withValg(JsonAdresseValg valg) {
            this.valg = valg;
            return this;
        }

        public AdresserFrontend withFolkeregistrert(AdresseFrontend folkeregistrert) {
            this.folkeregistrert = folkeregistrert;
            return this;
        }

        public AdresserFrontend withMidlertidig(AdresseFrontend midlertidig) {
            this.midlertidig = midlertidig;
            return this;
        }

        public AdresserFrontend withSoknad(AdresseFrontend soknad) {
            this.soknad = soknad;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AdresseFrontend {
        public JsonAdresse.Type type;
        public GateadresseFrontend gateadresse;
        public MatrikkeladresseFrontend matrikkeladresse;
        public UstrukturertAdresseFrontend ustrukturert;

        public void setType(JsonAdresse.Type type) {
            this.type = type;
        }

        public void setGateadresse(GateadresseFrontend gateadresse) {
            this.gateadresse = gateadresse;
        }

        public void setMatrikkeladresse(MatrikkeladresseFrontend matrikkeladresse) {
            this.matrikkeladresse = matrikkeladresse;
        }

        public void setUstrukturert(UstrukturertAdresseFrontend ustrukturert) {
            this.ustrukturert = ustrukturert;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class GateadresseFrontend {
        public String landkode;
        public String kommunenummer;
        public List<String> adresselinjer;
        public String bolignummer;
        public String postnummer;
        public String poststed;
        public String gatenavn;
        public String husnummer;
        public String husbokstav;

        public GateadresseFrontend withLandkode(String landkode) {
            this.landkode = landkode;
            return this;
        }

        public GateadresseFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        public GateadresseFrontend withAdresselinjer(List<String> adresselinjer) {
            this.adresselinjer = adresselinjer;
            return this;
        }

        public GateadresseFrontend withBolignummer(String bolignummer) {
            this.bolignummer = bolignummer;
            return this;
        }

        public GateadresseFrontend withPostnummer(String postnummer) {
            this.postnummer = postnummer;
            return this;
        }

        public GateadresseFrontend withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }

        public GateadresseFrontend withGatenavn(String gatenavn) {
            this.gatenavn = gatenavn;
            return this;
        }

        public GateadresseFrontend withHusbokstav(String husbokstav) {
            this.husbokstav = husbokstav;
            return this;
        }

        public GateadresseFrontend withHusnummer(String husnummer) {
            this.husnummer = husnummer;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class MatrikkeladresseFrontend {
        public String kommunenummer;
        public String gaardsnummer;
        public String bruksnummer;
        public String festenummer;
        public String seksjonsnummer;
        public String undernummer;

        public MatrikkeladresseFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        public MatrikkeladresseFrontend withGaardsnummer(String gaardsnummer) {
            this.gaardsnummer = gaardsnummer;
            return this;
        }

        public MatrikkeladresseFrontend withBruksnummer(String bruksnummer) {
            this.bruksnummer = bruksnummer;
            return this;
        }

        public MatrikkeladresseFrontend withFestenummer(String festenummer) {
            this.festenummer = festenummer;
            return this;
        }

        public MatrikkeladresseFrontend withSeksjonsnummer(String seksjonsnummer) {
            this.seksjonsnummer = seksjonsnummer;
            return this;
        }

        public MatrikkeladresseFrontend withUndernummer(String undernummer) {
            this.undernummer = undernummer;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class UstrukturertAdresseFrontend {
        public List<String> adresse;

        public UstrukturertAdresseFrontend withAdresse(List<String> adresse) {
            this.adresse = adresse;
            return this;
        }
    }
}
