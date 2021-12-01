package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.personalia.person.PersonService;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Kontaktadresse;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse;
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AdresseSystemdata implements Systemdata {

    private static final Logger log = getLogger(AdresseSystemdata.class);

    private final PersonService personService;

    public AdresseSystemdata(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        JsonPersonalia personalia = soknad.getData().getPersonalia();
        String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        JsonAdresse folkeregistrertAdresse = innhentFolkeregistrertAdresse(personIdentifikator);
        JsonAdresse midlertidigAdresse = innhentMidlertidigAdresse(personIdentifikator);
        if (valgtAdresseLikNull(personalia, folkeregistrertAdresse, midlertidigAdresse)) {
            personalia.setOppholdsadresse(null);
            personalia.setPostadresse(null);
            soknad.setMottaker(new JsonSoknadsmottaker());
        }
        personalia.setFolkeregistrertAdresse(folkeregistrertAdresse);
        updateOppholdsadresse(personalia, folkeregistrertAdresse, midlertidigAdresse);
        updatePostadresse(personalia, folkeregistrertAdresse, midlertidigAdresse);
    }

    private boolean valgtAdresseLikNull(JsonPersonalia personalia, JsonAdresse folkeregistrertAdresse, JsonAdresse midlertidigAdresse) {
        return folkeregistrertAdresse == null && personalia.getOppholdsadresse() != null &&
                JsonAdresseValg.FOLKEREGISTRERT.equals(personalia.getOppholdsadresse().getAdresseValg())
                || midlertidigAdresse == null && personalia.getOppholdsadresse() != null &&
                JsonAdresseValg.MIDLERTIDIG.equals(personalia.getOppholdsadresse().getAdresseValg());
    }

    private void updatePostadresse(JsonPersonalia personalia, JsonAdresse folkeregistrertAdresse, JsonAdresse midlertidigAdresse) {
        JsonAdresse postadresse = personalia.getPostadresse();
        if (postadresse == null){
            return;
        }
        JsonAdresseValg adresseValg = postadresse.getAdresseValg();
        if (adresseValg == JsonAdresseValg.FOLKEREGISTRERT){
            personalia.setPostadresse(createDeepCopyOfJsonAdresse(folkeregistrertAdresse));
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG){
            personalia.setPostadresse(createDeepCopyOfJsonAdresse(midlertidigAdresse).withAdresseValg(adresseValg));
        }
    }

    private void updateOppholdsadresse(JsonPersonalia personalia, JsonAdresse folkeregistrertAdresse, JsonAdresse midlertidigAdresse) {
        JsonAdresse oppholdsadresse = personalia.getOppholdsadresse();
        if (oppholdsadresse == null){
            return;
        }
        JsonAdresseValg adresseValg = oppholdsadresse.getAdresseValg();
        if (adresseValg == JsonAdresseValg.FOLKEREGISTRERT){
            personalia.setOppholdsadresse(createDeepCopyOfJsonAdresse(folkeregistrertAdresse).withAdresseValg(adresseValg));
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG){
            personalia.setOppholdsadresse(createDeepCopyOfJsonAdresse(midlertidigAdresse).withAdresseValg(adresseValg));
        }
    }

    public JsonAdresse innhentFolkeregistrertAdresse(String personIdentifikator) {
        var person = personService.hentPerson(personIdentifikator);
        return mapToJsonAdresse(person.getBostedsadresse());
    }

    public JsonAdresse innhentMidlertidigAdresse(String personIdentifikator) {
        var person = personService.hentPerson(personIdentifikator);
        return mapToJsonAdresse(person.getOppholdsadresse());
//        return mapToJsonAdresse(person.getKontaktadresse());
    }

    private JsonAdresse mapToJsonAdresse(Bostedsadresse bostedsadresse) {
        if (bostedsadresse == null) {
            return null;
        }

        JsonAdresse jsonAdresse;

        if (bostedsadresse.getVegadresse() != null) {
            jsonAdresse = tilGateAdresse(bostedsadresse.getVegadresse());
        } else if (bostedsadresse.getMatrikkeladresse() != null) {
            jsonAdresse = tilMatrikkelAdresse(bostedsadresse.getMatrikkeladresse());
        } else {
            throw new IllegalStateException("Ukjent bostedsadresse fra PDL (skal være Vegadresse eller Matrikkeladresse");
        }

        jsonAdresse.setKilde(JsonKilde.SYSTEM);

        return jsonAdresse;
    }

    private JsonAdresse mapToJsonAdresse(Oppholdsadresse oppholdsadresse) {
        if (oppholdsadresse == null) {
            return null;
        }

        JsonAdresse jsonAdresse;

        if (oppholdsadresse.getVegadresse() != null) {
            jsonAdresse = tilGateAdresse(oppholdsadresse.getVegadresse());
        }
        // legg til matrikkeladresse hvis man henter oppholdsadresse som er matrikkeladresse
        else {
            throw new IllegalStateException("Ukjent oppholdsadresse fra PDL (skal være Vegadresse)");
        }

        jsonAdresse.setKilde(JsonKilde.SYSTEM);

        return jsonAdresse;
    }

    private JsonAdresse mapToJsonAdresse(Kontaktadresse kontaktadresse) {
        if (kontaktadresse == null) {
            return null;
        }

        if (kontaktadresse.getVegadresse() != null) {
            var jsonAdresse = tilGateAdresse(kontaktadresse.getVegadresse());
            jsonAdresse.setKilde(JsonKilde.SYSTEM);
            return jsonAdresse;
        } else {
            throw new IllegalStateException("Ukjent kontaktadresse fra PDL (skal være Vegadresse)");
        }
    }

    private JsonGateAdresse tilGateAdresse(Vegadresse vegadresse) {
        var jsonGateAdresse = new JsonGateAdresse();
        jsonGateAdresse.setType(JsonAdresse.Type.GATEADRESSE);
        jsonGateAdresse.setLandkode("NOR"); // vegadresser er kun norske
        jsonGateAdresse.setKommunenummer(vegadresse.getKommunenummer());
        jsonGateAdresse.setBolignummer(vegadresse.getBruksenhetsnummer());
        jsonGateAdresse.setGatenavn(vegadresse.getAdressenavn());
        jsonGateAdresse.setHusnummer(vegadresse.getHusnummer() == null ? null : vegadresse.getHusnummer().toString());
        jsonGateAdresse.setHusbokstav(vegadresse.getHusbokstav());
        jsonGateAdresse.setPostnummer(vegadresse.getPostnummer());
        jsonGateAdresse.setPoststed(vegadresse.getPoststed());
        return jsonGateAdresse;
    }

    private JsonMatrikkelAdresse tilMatrikkelAdresse(Matrikkeladresse matrikkeladresse) {
        var jsonMatrikkelAdresse = new JsonMatrikkelAdresse();
        jsonMatrikkelAdresse.setType(JsonAdresse.Type.MATRIKKELADRESSE);
        jsonMatrikkelAdresse.setKommunenummer(matrikkeladresse.getKommunenummer());
        jsonMatrikkelAdresse.setBruksnummer(matrikkeladresse.getBruksenhetsnummer());
        return jsonMatrikkelAdresse;
    }

    public JsonAdresse createDeepCopyOfJsonAdresse(JsonAdresse oppholdsadresse) {
        switch (oppholdsadresse.getType()){
            case GATEADRESSE:
                return new JsonGateAdresse()
                        .withKilde(oppholdsadresse.getKilde())
                        .withAdresseValg(oppholdsadresse.getAdresseValg())
                        .withType(oppholdsadresse.getType())
                        .withLandkode(((JsonGateAdresse) oppholdsadresse).getLandkode())
                        .withKommunenummer(((JsonGateAdresse) oppholdsadresse).getKommunenummer())
                        .withBolignummer(((JsonGateAdresse) oppholdsadresse).getBolignummer())
                        .withGatenavn(((JsonGateAdresse) oppholdsadresse).getGatenavn())
                        .withHusnummer(((JsonGateAdresse) oppholdsadresse).getHusnummer())
                        .withHusbokstav(((JsonGateAdresse) oppholdsadresse).getHusbokstav())
                        .withPostnummer(((JsonGateAdresse) oppholdsadresse).getPostnummer())
                        .withPoststed(((JsonGateAdresse) oppholdsadresse).getPoststed());
            case MATRIKKELADRESSE:
                return new JsonMatrikkelAdresse()
                        .withKilde(oppholdsadresse.getKilde())
                        .withAdresseValg(oppholdsadresse.getAdresseValg())
                        .withType(oppholdsadresse.getType())
                        .withKommunenummer(((JsonMatrikkelAdresse) oppholdsadresse).getKommunenummer())
                        .withGaardsnummer(((JsonMatrikkelAdresse) oppholdsadresse).getGaardsnummer())
                        .withBruksnummer(((JsonMatrikkelAdresse) oppholdsadresse).getBruksnummer())
                        .withFestenummer(((JsonMatrikkelAdresse) oppholdsadresse).getFestenummer())
                        .withSeksjonsnummer(((JsonMatrikkelAdresse) oppholdsadresse).getSeksjonsnummer())
                        .withUndernummer(((JsonMatrikkelAdresse) oppholdsadresse).getUndernummer());
            case USTRUKTURERT:
                return new JsonUstrukturertAdresse()
                        .withKilde(oppholdsadresse.getKilde())
                        .withAdresseValg(oppholdsadresse.getAdresseValg())
                        .withType(oppholdsadresse.getType())
                        .withAdresse(((JsonUstrukturertAdresse) oppholdsadresse).getAdresse());
            case POSTBOKS:
                return new JsonPostboksAdresse()
                        .withKilde(oppholdsadresse.getKilde())
                        .withAdresseValg(oppholdsadresse.getAdresseValg())
                        .withType(oppholdsadresse.getType())
                        .withPostboks(((JsonPostboksAdresse) oppholdsadresse).getPostboks())
                        .withPostnummer(((JsonPostboksAdresse) oppholdsadresse).getPostnummer())
                        .withPoststed(((JsonPostboksAdresse) oppholdsadresse).getPoststed());
            default:
                return null;
        }
    }
}
