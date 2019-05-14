package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class AdresseSystemdata implements Systemdata {

    @Inject
    private PersonaliaFletter personaliaFletter;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        final JsonAdresse folkeregistrertAdresse = innhentFolkeregistrertAdresse(personIdentifikator);
        final JsonAdresse midlertidigAdresse = innhentMidlertidigAdresse(personIdentifikator);
        personalia.setFolkeregistrertAdresse(folkeregistrertAdresse);
        updateOppholdsadresse(personalia, folkeregistrertAdresse, midlertidigAdresse);
        updatePostadresse(personalia, folkeregistrertAdresse, midlertidigAdresse);
    }

    private void updatePostadresse(JsonPersonalia personalia, JsonAdresse folkeregistrertAdresse, JsonAdresse midlertidigAdresse) {
        final JsonAdresse postadresse = personalia.getPostadresse();
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
        final JsonAdresse oppholdsadresse = personalia.getOppholdsadresse();
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

    public JsonAdresse innhentFolkeregistrertAdresse(final String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        return mapToJsonAdresse(personalia.getFolkeregistrertAdresse());
    }

    public JsonAdresse innhentMidlertidigAdresse(final String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        return mapToJsonAdresse(personalia.getMidlertidigAdresse());
    }

    private JsonAdresse mapToJsonAdresse(Adresse adresse) {
        if (adresse == null || isUtenlandskAdresse(adresse)) {
            return null;
        }

        final Adresse.StrukturertAdresse strukturertAdresse = adresse.getStrukturertAdresse();

        if (strukturertAdresse == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            return null;
        }

        final String type = adresse.getAdressetype();
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

    private static JsonAdresse tilGateAdresse(final Adresse adresse) {
        if (adresse.getStrukturertAdresse() == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            throw new IllegalStateException("Adresse er ikke strukturert");
        }

        final Adresse.Gateadresse gateadresse = (Adresse.Gateadresse) adresse.getStrukturertAdresse();
        final JsonGateAdresse jsonGateAdresse = new JsonGateAdresse();
        jsonGateAdresse.setType(JsonAdresse.Type.GATEADRESSE);
        jsonGateAdresse.setLandkode(temporaryFixForLandkode(adresse));
        jsonGateAdresse.setKommunenummer(nullIfEmpty(gateadresse.kommunenummer));
        jsonGateAdresse.setBolignummer(nullIfEmpty(gateadresse.bolignummer));
        jsonGateAdresse.setGatenavn(nullIfEmpty(gateadresse.gatenavn));
        jsonGateAdresse.setHusnummer(nullIfEmpty(gateadresse.husnummer));
        jsonGateAdresse.setHusbokstav(nullIfEmpty(gateadresse.husbokstav));
        jsonGateAdresse.setPostnummer(nullIfEmpty(gateadresse.postnummer));
        jsonGateAdresse.setPoststed(nullIfEmpty(gateadresse.poststed));
        return jsonGateAdresse;
    }

    private static String temporaryFixForLandkode(Adresse adresse) {
        return nullIfEmpty(adresse.getLandkode()) == null ? "NOR" : adresse.getLandkode();
    }

    private static JsonAdresse tilMatrikkelAdresse(final Adresse adresse) {
        if (adresse.getStrukturertAdresse() == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            throw new IllegalStateException("Adresse er ikke strukturert");
        }

        final Adresse.MatrikkelAdresse matrikkelAdresse = (Adresse.MatrikkelAdresse) adresse.getStrukturertAdresse();
        final JsonMatrikkelAdresse jsonMatrikkelAdresse = new JsonMatrikkelAdresse();
        jsonMatrikkelAdresse.setType(JsonAdresse.Type.MATRIKKELADRESSE);
        jsonMatrikkelAdresse.setKommunenummer(nullIfEmpty(matrikkelAdresse.kommunenummer));
        jsonMatrikkelAdresse.setGaardsnummer(nullIfEmpty(matrikkelAdresse.gaardsnummer));
        jsonMatrikkelAdresse.setBruksnummer(nullIfEmpty(matrikkelAdresse.bruksnummer));
        jsonMatrikkelAdresse.setFestenummer(nullIfEmpty(matrikkelAdresse.festenummer));
        jsonMatrikkelAdresse.setSeksjonsnummer(nullIfEmpty(matrikkelAdresse.seksjonsnummer));
        jsonMatrikkelAdresse.setUndernummer(nullIfEmpty(matrikkelAdresse.undernummer));
        return jsonMatrikkelAdresse;
    }

    private static JsonAdresse tilUstrukturertAdresse(Adresse adresse) {
        if (adresse.getAdresse() == null) {
            return null;
        }

        final JsonUstrukturertAdresse ustrukturertAdresse = new JsonUstrukturertAdresse();
        ustrukturertAdresse.setType(JsonAdresse.Type.USTRUKTURERT);

        ustrukturertAdresse.setAdresse(Arrays.stream(adresse.getAdresse().split(","))
                .map(s -> s.trim())
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

    private boolean isUtenlandskAdresse(final Adresse adresse) {
        return adresse.getLandkode() != null && !adresse.getLandkode().equals("NOR");
    }

    private static String nullIfEmpty(String s) {
        if (s != null && s.trim().equals("")) {
            return null;
        }
        return s;
    }
}
