package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.finn.unleash.Unleash;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
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
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.consumer.pdlperson.PersonSammenligner;
import no.nav.sosialhjelp.soknad.consumer.personv3.PersonServiceV3;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.Adresse;
import no.nav.sosialhjelp.soknad.domain.model.AdresserOgKontonummer;
import no.nav.sosialhjelp.soknad.domain.model.Bostedsadresse;
import no.nav.sosialhjelp.soknad.domain.model.Kontaktadresse;
import no.nav.sosialhjelp.soknad.domain.model.Matrikkeladresse;
import no.nav.sosialhjelp.soknad.domain.model.Oppholdsadresse;
import no.nav.sosialhjelp.soknad.domain.model.Vegadresse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AdresseSystemdata implements Systemdata {

    private static final Logger log = getLogger(AdresseSystemdata.class);
    public static final String FEATURE_ADRESSER_PDL = "sosialhjelp.soknad.adresser-pdl";

    private final PersonServiceV3 personService;
    private final PdlService pdlService;
    private final PersonSammenligner personSammenligner;
    private final Unleash unleashConsumer;

    public AdresseSystemdata(PersonServiceV3 personService, PdlService pdlService, PersonSammenligner personSammenligner, Unleash unleashConsumer) {
        this.personService = personService;
        this.pdlService = pdlService;
        this.personSammenligner = personSammenligner;
        this.unleashConsumer = unleashConsumer;
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
        var skalHenteAdresserFraPdl = unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false);
        if (skalHenteAdresserFraPdl) {
            var person = pdlService.hentPerson(personIdentifikator);
            return mapToJsonAdresse(person.getBostedsadresse());
        }
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer(personIdentifikator);
        sammenlignFolkeregistrertAdresse(personIdentifikator, adresserOgKontonummer.getFolkeregistrertAdresse());
        return mapToJsonAdresse(adresserOgKontonummer.getFolkeregistrertAdresse());
    }

    public JsonAdresse innhentMidlertidigAdresse(String personIdentifikator) {
        var skalHenteAdresserFraPdl = unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false);
        if (skalHenteAdresserFraPdl) {
            var person = pdlService.hentPerson(personIdentifikator);
//            return mapToJsonAdresse(person.getOppholdsadresse());
            return mapToJsonAdresse(person.getKontaktadresse());
        }
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer(personIdentifikator);
        sammenlignMidlertidigAdresse(personIdentifikator, adresserOgKontonummer.getMidlertidigAdresse());
        return mapToJsonAdresse(adresserOgKontonummer.getMidlertidigAdresse());
    }

    private void sammenlignFolkeregistrertAdresse(String personIdentifikator, Adresse folkeregistrertAdresse) {
        try {
            var person = pdlService.hentPerson(personIdentifikator);
            personSammenligner.sammenlignFolkeregistrertAdresse(folkeregistrertAdresse, person.getBostedsadresse());
        } catch (PdlApiException | TjenesteUtilgjengeligException e) {
            log.warn("PDL kaster feil (brukes kun for sammenligning)", e);
        } catch (Exception e) {
            log.warn("PDL-feil eller feil ved sammenligning av data fra TPS/PDL", e);
        }
    }

    private void sammenlignMidlertidigAdresse(String personIdentifikator, Adresse midlertidigAdresse) {
        try {
            var person = pdlService.hentPerson(personIdentifikator);
            personSammenligner.sammenlignMidlertidigAdresseOppholdsadresse(midlertidigAdresse, person.getOppholdsadresse());
            personSammenligner.sammenlignMidlertidigAdresseKontaktadresse(midlertidigAdresse, person.getKontaktadresse());
        } catch (PdlApiException | TjenesteUtilgjengeligException e) {
            log.warn("PDL kaster feil (brukes kun for sammenligning)", e);
        } catch (Exception e) {
            log.warn("PDL-feil eller feil ved sammenligning av data fra TPS/PDL", e);
        }
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

    private JsonAdresse mapToJsonAdresse(Adresse adresse) {
        if (adresse == null || isUtenlandskAdresse(adresse)) {
            return null;
        }

        Adresse.StrukturertAdresse strukturertAdresse = adresse.getStrukturertAdresse();

        if (strukturertAdresse == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            return null;
        }

        String type = adresse.getAdressetype();
        if (type == null) {
            throw new IllegalStateException("Adresse mangler type");
        }

        JsonAdresse jsonAdresse;
        if (strukturertAdresse instanceof Adresse.Gateadresse) {
            jsonAdresse = tilGateAdresse(adresse);
        } else if (strukturertAdresse instanceof Adresse.MatrikkelAdresse) {
            jsonAdresse = tilMatrikkelAdresse(adresse);
        } else if (type.equals("ustrukturert")) {
            jsonAdresse = tilUstrukturertAdresse(adresse);
        } else if (type.equals("postboks")) {
            throw new IllegalStateException("Adresser av typen \"postboks\" har ikke blitt implementert ennå.");
        } else {
            throw new IllegalStateException("Ukjent adressetype: \"" + type + "\"");
        }

        jsonAdresse.setKilde(JsonKilde.SYSTEM);

        return jsonAdresse;
    }

    private static JsonAdresse tilGateAdresse(Adresse adresse) {
        if (adresse.getStrukturertAdresse() == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            throw new IllegalStateException("Adresse er ikke strukturert");
        }

        Adresse.Gateadresse gateadresse = (Adresse.Gateadresse) adresse.getStrukturertAdresse();
        JsonGateAdresse jsonGateAdresse = new JsonGateAdresse();
        jsonGateAdresse.setType(JsonAdresse.Type.GATEADRESSE);
        jsonGateAdresse.setLandkode(defaultIfBlank(adresse.getLandkode(), "NOR"));
        jsonGateAdresse.setKommunenummer(defaultIfBlank(gateadresse.kommunenummer, null));
        jsonGateAdresse.setBolignummer(defaultIfBlank(gateadresse.bolignummer, null));
        jsonGateAdresse.setGatenavn(defaultIfBlank(gateadresse.gatenavn, null));
        jsonGateAdresse.setHusnummer(defaultIfBlank(gateadresse.husnummer, null));
        jsonGateAdresse.setHusbokstav(defaultIfBlank(gateadresse.husbokstav, null));
        jsonGateAdresse.setPostnummer(defaultIfBlank(gateadresse.postnummer, null));
        jsonGateAdresse.setPoststed(defaultIfBlank(gateadresse.poststed, null));
        return jsonGateAdresse;
    }

    private static JsonAdresse tilMatrikkelAdresse(Adresse adresse) {
        if (adresse.getStrukturertAdresse() == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            throw new IllegalStateException("Adresse er ikke strukturert");
        }

        Adresse.MatrikkelAdresse matrikkelAdresse = (Adresse.MatrikkelAdresse) adresse.getStrukturertAdresse();
        JsonMatrikkelAdresse jsonMatrikkelAdresse = new JsonMatrikkelAdresse();
        jsonMatrikkelAdresse.setType(JsonAdresse.Type.MATRIKKELADRESSE);
        jsonMatrikkelAdresse.setKommunenummer(defaultIfBlank(matrikkelAdresse.kommunenummer, null));
        jsonMatrikkelAdresse.setGaardsnummer(defaultIfBlank(matrikkelAdresse.gaardsnummer, null));
        jsonMatrikkelAdresse.setBruksnummer(defaultIfBlank(matrikkelAdresse.bruksnummer, null));
        jsonMatrikkelAdresse.setFestenummer(defaultIfBlank(matrikkelAdresse.festenummer, null));
        jsonMatrikkelAdresse.setSeksjonsnummer(defaultIfBlank(matrikkelAdresse.seksjonsnummer, null));
        jsonMatrikkelAdresse.setUndernummer(defaultIfBlank(matrikkelAdresse.undernummer, null));
        return jsonMatrikkelAdresse;
    }

    private static JsonAdresse tilUstrukturertAdresse(Adresse adresse) {
        if (adresse.getAdresse() == null) {
            return null;
        }

        JsonUstrukturertAdresse ustrukturertAdresse = new JsonUstrukturertAdresse();
        ustrukturertAdresse.setType(JsonAdresse.Type.USTRUKTURERT);

        ustrukturertAdresse.setAdresse(Arrays.stream(adresse.getAdresse().split(","))
                .map(String::trim)
                .collect(Collectors.toList()));

        return ustrukturertAdresse;
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

    private boolean isUtenlandskAdresse(Adresse adresse) {
        return adresse.getLandkode() != null && !adresse.getLandkode().equals("NOR");
    }
}
