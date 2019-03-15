package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.personalia.AdresseRessurs;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;

public class AdresseMapper {
    public static AdresseRessurs.AdresserFrontend mapToAdresserFrontend(JsonAdresse sysFolkeregistrert, JsonAdresse sysMidlertidig, JsonAdresse jsonOpphold) {
        return new AdresseRessurs.AdresserFrontend()
                .withValg(jsonOpphold != null ? jsonOpphold.getAdresseValg() : null)
                .withFolkeregistrert(mapToAdresseFrontend(sysFolkeregistrert))
                .withMidlertidig(mapToAdresseFrontend(sysMidlertidig))
                .withSoknad(mapToAdresseFrontend(jsonOpphold));
    }

    private static AdresseRessurs.AdresseFrontend mapToAdresseFrontend(JsonAdresse adresse) {
        if (adresse == null){
            return null;
        }
        AdresseRessurs.AdresseFrontend adresseFrontend = new AdresseRessurs.AdresseFrontend();
        switch (adresse.getType()){
            case GATEADRESSE:
                adresseFrontend.setType(JsonAdresse.Type.GATEADRESSE);
                adresseFrontend.setGateadresse(mapToGateadresseFrontend(adresse));
                break;
            case MATRIKKELADRESSE:
                adresseFrontend.setType(JsonAdresse.Type.MATRIKKELADRESSE);
                adresseFrontend.setMatrikkeladresse(mapToMatrikkeladresseFrontend(adresse));
                break;
            case USTRUKTURERT:
                adresseFrontend.setType(JsonAdresse.Type.USTRUKTURERT);
                adresseFrontend.setUstrukturert(mapToUstrukturertAdresseFrontend(adresse));
                break;
        }
        return adresseFrontend;
    }

    private static AdresseRessurs.GateadresseFrontend mapToGateadresseFrontend(JsonAdresse adresse) {
        JsonGateAdresse gateAdresse = (JsonGateAdresse) adresse;
        return new AdresseRessurs.GateadresseFrontend()
                .withLandkode(gateAdresse.getLandkode())
                .withKommunenummer(gateAdresse.getKommunenummer())
                .withAdresselinjer(gateAdresse.getAdresselinjer())
                .withBolignummer(gateAdresse.getBolignummer())
                .withPostnummer(gateAdresse.getPostnummer())
                .withPoststed(gateAdresse.getPoststed())
                .withGatenavn(gateAdresse.getGatenavn())
                .withHusnummer(gateAdresse.getHusnummer())
                .withHusbokstav(gateAdresse.getHusbokstav());
    }

    private static AdresseRessurs.MatrikkeladresseFrontend mapToMatrikkeladresseFrontend(JsonAdresse adresse) {
        JsonMatrikkelAdresse matrikkelAdresse = (JsonMatrikkelAdresse) adresse;
        return new AdresseRessurs.MatrikkeladresseFrontend()
                .withKommunenummer(matrikkelAdresse.getKommunenummer())
                .withGaardsnummer(matrikkelAdresse.getGaardsnummer())
                .withBruksnummer(matrikkelAdresse.getBruksnummer())
                .withFestenummer(matrikkelAdresse.getFestenummer())
                .withSeksjonsnummer(matrikkelAdresse.getSeksjonsnummer())
                .withUndernummer(matrikkelAdresse.getUndernummer());
    }

    private static AdresseRessurs.UstrukturertAdresseFrontend mapToUstrukturertAdresseFrontend(JsonAdresse adresse){
        JsonUstrukturertAdresse ustrukturertAdresse = (JsonUstrukturertAdresse) adresse;
        return new AdresseRessurs.UstrukturertAdresseFrontend().withAdresse(ustrukturertAdresse.getAdresse());
    }

    public static JsonAdresse mapToJsonAdresse(AdresseRessurs.AdresseFrontend adresseFrontend) {
        JsonAdresse adresse;
        switch (adresseFrontend.type){
            case GATEADRESSE:
                AdresseRessurs.GateadresseFrontend gateadresse = adresseFrontend.gateadresse;
                adresse = new JsonGateAdresse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(JsonAdresse.Type.GATEADRESSE)
                        .withLandkode(gateadresse.landkode)
                        .withKommunenummer(gateadresse.kommunenummer)
                        .withAdresselinjer(gateadresse.adresselinjer)
                        .withBolignummer(gateadresse.bolignummer)
                        .withPostnummer(gateadresse.postnummer)
                        .withPoststed(gateadresse.poststed)
                        .withGatenavn(gateadresse.gatenavn)
                        .withHusnummer(gateadresse.husnummer)
                        .withHusbokstav(gateadresse.husbokstav);
                break;
            case MATRIKKELADRESSE:
                AdresseRessurs.MatrikkeladresseFrontend matrikkeladresse = adresseFrontend.matrikkeladresse;
                adresse = new JsonMatrikkelAdresse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                        .withKommunenummer(matrikkeladresse.kommunenummer)
                        .withGaardsnummer(matrikkeladresse.gaardsnummer)
                        .withBruksnummer(matrikkeladresse.bruksnummer)
                        .withFestenummer(matrikkeladresse.festenummer)
                        .withSeksjonsnummer(matrikkeladresse.seksjonsnummer)
                        .withUndernummer(matrikkeladresse.undernummer);
                break;
            default:
                throw new IllegalStateException("Ukjent adressetype: \"" + adresseFrontend.type + "\".");
        }
        return adresse;
    }
}
