package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.mappers.AdresseMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/personalia/adresser")
@Timed
@Produces(APPLICATION_JSON)
public class AdresseRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private AdresseSystemdata adresseSystemdata;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private NavEnhetRessurs navEnhetRessurs;

    @GET
    public AdresserFrontend hentAdresser(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserIdFromToken();
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
    public List<NavEnhetRessurs.NavEnhetFrontend> updateAdresse(@PathParam("behandlingsId") String behandlingsId, AdresserFrontend adresserFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserIdFromToken();
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
        return navEnhetRessurs.findSoknadsmottaker(soknad.getJsonInternalSoknad().getSoknad(), adresserFrontend.valg.toString(), null);
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
