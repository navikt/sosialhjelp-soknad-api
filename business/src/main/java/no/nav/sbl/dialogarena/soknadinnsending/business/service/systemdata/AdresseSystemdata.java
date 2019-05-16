package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

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
            personalia.setPostadresse(folkeregistrertAdresse);
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG){
            personalia.setPostadresse(midlertidigAdresse);
        }
    }

    private void updateOppholdsadresse(JsonPersonalia personalia, JsonAdresse folkeregistrertAdresse, JsonAdresse midlertidigAdresse) {
        final JsonAdresse oppholdsadresse = personalia.getOppholdsadresse();
        if (oppholdsadresse == null){
            return;
        }
        JsonAdresseValg adresseValg = oppholdsadresse.getAdresseValg();
        if (adresseValg == JsonAdresseValg.FOLKEREGISTRERT){
            personalia.setOppholdsadresse(folkeregistrertAdresse);
            personalia.getOppholdsadresse().setAdresseValg(adresseValg);
        }
        if (adresseValg == JsonAdresseValg.MIDLERTIDIG){
            personalia.setOppholdsadresse(midlertidigAdresse);
            personalia.getOppholdsadresse().setAdresseValg(adresseValg);
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
        jsonGateAdresse.setKommunenummer(defaultIfBlank(gateadresse.kommunenummer, null));
        jsonGateAdresse.setBolignummer(defaultIfBlank(gateadresse.bolignummer, null));
        jsonGateAdresse.setGatenavn(defaultIfBlank(gateadresse.gatenavn, null));
        jsonGateAdresse.setHusnummer(defaultIfBlank(gateadresse.husnummer, null));
        jsonGateAdresse.setHusbokstav(defaultIfBlank(gateadresse.husbokstav, null));
        jsonGateAdresse.setPostnummer(defaultIfBlank(gateadresse.postnummer, null));
        jsonGateAdresse.setPoststed(defaultIfBlank(gateadresse.poststed, null));
        return jsonGateAdresse;
    }

    private static String temporaryFixForLandkode(Adresse adresse) {
        return defaultIfBlank(adresse.getLandkode(), "NOR");
    }

    private static JsonAdresse tilMatrikkelAdresse(final Adresse adresse) {
        if (adresse.getStrukturertAdresse() == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            throw new IllegalStateException("Adresse er ikke strukturert");
        }

        final Adresse.MatrikkelAdresse matrikkelAdresse = (Adresse.MatrikkelAdresse) adresse.getStrukturertAdresse();
        final JsonMatrikkelAdresse jsonMatrikkelAdresse = new JsonMatrikkelAdresse();
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
