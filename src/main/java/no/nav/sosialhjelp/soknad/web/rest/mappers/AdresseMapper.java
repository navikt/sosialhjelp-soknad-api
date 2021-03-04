package no.nav.sosialhjelp.soknad.web.rest.mappers;

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.AdresseRessurs;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public final class AdresseMapper {

    private AdresseMapper() {
    }

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
                        .withLandkode(defaultIfBlank(gateadresse.landkode, "NOR"))
                        .withKommunenummer(defaultIfBlank(gateadresse.kommunenummer, null))
                        .withAdresselinjer(gateadresse.adresselinjer)
                        .withBolignummer(defaultIfBlank(gateadresse.bolignummer, null))
                        .withPostnummer(defaultIfBlank(gateadresse.postnummer, null))
                        .withPoststed(defaultIfBlank(gateadresse.poststed, null))
                        .withGatenavn(defaultIfBlank(gateadresse.gatenavn, null))
                        .withHusnummer(defaultIfBlank(gateadresse.husnummer, null))
                        .withHusbokstav(defaultIfBlank(gateadresse.husbokstav, null));
                break;
            case MATRIKKELADRESSE:
                AdresseRessurs.MatrikkeladresseFrontend matrikkeladresse = adresseFrontend.matrikkeladresse;
                adresse = new JsonMatrikkelAdresse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                        .withKommunenummer(defaultIfBlank(matrikkeladresse.kommunenummer, null))
                        .withGaardsnummer(defaultIfBlank(matrikkeladresse.gaardsnummer, null))
                        .withBruksnummer(defaultIfBlank(matrikkeladresse.bruksnummer, null))
                        .withFestenummer(defaultIfBlank(matrikkeladresse.festenummer, null))
                        .withSeksjonsnummer(defaultIfBlank(matrikkeladresse.seksjonsnummer, null))
                        .withUndernummer(defaultIfBlank(matrikkeladresse.undernummer, null));
                break;
            default:
                throw new IllegalStateException("Ukjent adressetype: \"" + adresseFrontend.type + "\".");
        }
        return adresse;
    }
}
