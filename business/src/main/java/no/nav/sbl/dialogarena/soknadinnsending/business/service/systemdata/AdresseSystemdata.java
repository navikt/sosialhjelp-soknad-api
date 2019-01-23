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
        if (postadresse.getAdresseValg() == JsonAdresseValg.FOLKEREGISTRERT){
            personalia.setPostadresse(folkeregistrertAdresse);
        }
        if (postadresse.getAdresseValg() == JsonAdresseValg.MIDLERTIDIG){
            personalia.setPostadresse(midlertidigAdresse);
        }
    }

    private void updateOppholdsadresse(JsonPersonalia personalia, JsonAdresse folkeregistrertAdresse, JsonAdresse midlertidigAdresse) {
        final JsonAdresse oppholdsadresse = personalia.getOppholdsadresse();
        if (oppholdsadresse == null){
            return;
        }
        if (oppholdsadresse.getAdresseValg() == JsonAdresseValg.FOLKEREGISTRERT){
            personalia.setOppholdsadresse(folkeregistrertAdresse);
        }
        if (oppholdsadresse.getAdresseValg() == JsonAdresseValg.MIDLERTIDIG){
            personalia.setOppholdsadresse(midlertidigAdresse);
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

        if (adresse.getStrukturertAdresse() == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            return null;
        }

        final String type = adresse.getAdressetype();
        if (type == null) {
            throw new IllegalStateException("Adresse mangler type");
        }

        JsonAdresse jsonAdresse;
        if (type.equals("gateadresse")) {
            jsonAdresse = tilGateAdresse(adresse);
        } else if (type.equals("matrikkeladresse")) {
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
        final Adresse.Gateadresse gateadresse = (Adresse.Gateadresse) adresse.getStrukturertAdresse();
        final JsonGateAdresse jsonGateAdresse = new JsonGateAdresse();
        jsonGateAdresse.setType(JsonAdresse.Type.GATEADRESSE);
        jsonGateAdresse.setLandkode(adresse.getLandkode());
        jsonGateAdresse.setKommunenummer(gateadresse.kommunenummer);
        jsonGateAdresse.setBolignummer(gateadresse.bolignummer);
        jsonGateAdresse.setGatenavn(gateadresse.gatenavn);
        jsonGateAdresse.setHusnummer(gateadresse.husnummer);
        jsonGateAdresse.setHusbokstav(gateadresse.husbokstav);
        jsonGateAdresse.setPostnummer(gateadresse.postnummer);
        jsonGateAdresse.setPoststed(gateadresse.poststed);
        return jsonGateAdresse;
    }

    private static JsonAdresse tilMatrikkelAdresse(final Adresse adresse) {
        final Adresse.MatrikkelAdresse matrikkelAdresse = (Adresse.MatrikkelAdresse) adresse.getStrukturertAdresse();
        final JsonMatrikkelAdresse jsonMatrikkelAdresse = new JsonMatrikkelAdresse();
        jsonMatrikkelAdresse.setType(JsonAdresse.Type.MATRIKKELADRESSE);
        jsonMatrikkelAdresse.setKommunenummer(matrikkelAdresse.kommunenummer);
        jsonMatrikkelAdresse.setGaardsnummer(matrikkelAdresse.gaardsnummer);
        jsonMatrikkelAdresse.setBruksnummer(matrikkelAdresse.bruksnummer);
        jsonMatrikkelAdresse.setFestenummer(matrikkelAdresse.festenummer);
        jsonMatrikkelAdresse.setSeksjonsnummer(matrikkelAdresse.seksjonsnummer);
        jsonMatrikkelAdresse.setUndernummer(matrikkelAdresse.undernummer);
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

    private boolean isUtenlandskAdresse(final Adresse adresse) {
        return adresse.getLandkode() != null && !adresse.getLandkode().equals("NOR");
    }
}
