package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson;

import no.nav.sosialhjelp.soknad.domain.model.Adresse;
import no.nav.sosialhjelp.soknad.domain.model.Bostedsadresse;
import no.nav.sosialhjelp.soknad.domain.model.Kontaktadresse;
import no.nav.sosialhjelp.soknad.domain.model.Oppholdsadresse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonSammenligner {

    private static final Logger log = getLogger(PersonSammenligner.class);

    public void sammenlignFolkeregistrertAdresse(Adresse folkeregistrertAdresse, Bostedsadresse bostedsadresse) {
        if (folkeregistrertAdresse != null && bostedsadresse != null) {
            List<String> ulikeFelter = new ArrayList<>();
            if (folkeregistrertAdresse.getAdressetype().equals("gateadresse") && bostedsadresse.getVegadresse() != null) {
                var gateadresse = (Adresse.Gateadresse) folkeregistrertAdresse.getStrukturertAdresse();
                if (!gateadresse.gatenavn.equalsIgnoreCase(bostedsadresse.getVegadresse().getAdressenavn())) {
                    ulikeFelter.add("Adressenavn");
                }
                if (!gateadresse.husnummer.equalsIgnoreCase(bostedsadresse.getVegadresse().getHusnummer().toString())) {
                    ulikeFelter.add("Husnummer");
                }
                if (gateadresse.husbokstav != null
                        && bostedsadresse.getVegadresse().getHusbokstav() != null
                        && !gateadresse.husbokstav.equalsIgnoreCase(bostedsadresse.getVegadresse().getHusbokstav())) {
                    ulikeFelter.add("Husbokstav");
                }
                if (gateadresse.bolignummer != null
                        && bostedsadresse.getVegadresse().getBruksenhetsnummer() != null
                        && !gateadresse.bolignummer.equalsIgnoreCase(bostedsadresse.getVegadresse().getBruksenhetsnummer())) {
                    ulikeFelter.add("Bruksenhetsnummer");
                }
                if (!gateadresse.postnummer.equalsIgnoreCase(bostedsadresse.getVegadresse().getPostnummer())) {
                    ulikeFelter.add("Postnummer");
                }
                if (!gateadresse.kommunenummer.equalsIgnoreCase(bostedsadresse.getVegadresse().getKommunenummer())) {
                    ulikeFelter.add("Husbokstav");
                }
            }
            if (folkeregistrertAdresse.getAdressetype().equalsIgnoreCase("matrikkeladresse") && bostedsadresse.getMatrikkeladresse() != null) {
                // sammenligner ikke matrikkeladresser (enda)
            }

            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i FolkeregistrertAdresse fra Person_v3 vs Bostedsadresse fra PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("FolkeregistrertAdresse fra Person_v3 og Bostedsadresse PDL er like");
            }
        }
    }

    public void sammenlignMidlertidigAdresseOppholdsadresse(Adresse midlertidigAdresse, Oppholdsadresse oppholdsadresse) {
        if (midlertidigAdresse == null && oppholdsadresse != null) {
            log.info("MidlertidigAdresse er null i Person_v3, men oppholdsadresse er satt i PDL");
        }

        if (midlertidigAdresse != null && oppholdsadresse == null) {
            log.info("MidlertidigAdresse er satt i Person_v3, men oppholdsadresse er null i PDL");
        }

        if (midlertidigAdresse != null && oppholdsadresse != null) {
            List<String> ulikeFelter = new ArrayList<>();
            if (midlertidigAdresse.getAdressetype() != null
                    && midlertidigAdresse.getAdressetype().equals("gateadresse")
                    && oppholdsadresse.getVegadresse() != null) {
                var gateadresse = (Adresse.Gateadresse) midlertidigAdresse.getStrukturertAdresse();
                var vegadresse = oppholdsadresse.getVegadresse();
                if (!gateadresse.gatenavn.equalsIgnoreCase(vegadresse.getAdressenavn())) {
                    ulikeFelter.add("Adressenavn");
                }
                if (!gateadresse.husnummer.equalsIgnoreCase(vegadresse.getHusnummer().toString())) {
                    ulikeFelter.add("Husnummer");
                }
                if (gateadresse.husbokstav != null
                        && vegadresse.getHusbokstav() != null
                        && !gateadresse.husbokstav.equalsIgnoreCase(vegadresse.getHusbokstav())) {
                    ulikeFelter.add("Husbokstav");
                }
                if (gateadresse.bolignummer != null
                        && vegadresse.getBruksenhetsnummer() != null
                        && !gateadresse.bolignummer.equalsIgnoreCase(vegadresse.getBruksenhetsnummer())) {
                    ulikeFelter.add("Bruksenhetsnummer");
                }
                if (!gateadresse.postnummer.equalsIgnoreCase(vegadresse.getPostnummer())) {
                    ulikeFelter.add("Postnummer");
                }
                if (!gateadresse.kommunenummer.equalsIgnoreCase(vegadresse.getKommunenummer())) {
                    ulikeFelter.add("Kommunenummer");
                }
            }
            if (midlertidigAdresse.getAdressetype() != null
                    && midlertidigAdresse.getAdressetype().equalsIgnoreCase("matrikkeladresse")) {
                log.info("MidlertidigAdresse i TPS er matrikkeladresse. Har ikke hentet matrikkeladresse for Oppholdsadresse i PDL.");
            }

            if (midlertidigAdresse.getAdressetype() != null) {
                log.info("Midlertidig adresse - ukjent adressetype {}", midlertidigAdresse.getAdressetype());
            }

            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i MidlertidigAdresse i Person_v3 vs Oppholdsadresse i PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("MidlertidigAdresse i Person_v3 og Oppholdsadresse i PDL er like");
            }
        }
    }

    public void sammenlignMidlertidigAdresseKontaktadresse(Adresse midlertidigAdresse, Kontaktadresse kontaktadresse) {
        if (midlertidigAdresse == null && kontaktadresse != null) {
            log.info("MidlertidigAdresse er null i Person_v3, men kontaktadresse er satt i PDL");
        }

        if (midlertidigAdresse != null && kontaktadresse == null) {
            log.info("MidlertidigAdresse er satt i Person_v3, men kontaktadresse er null i PDL");
        }

        if (midlertidigAdresse != null && kontaktadresse != null) {
            List<String> ulikeFelter = new ArrayList<>();
            if (midlertidigAdresse.getAdressetype() != null
                    && midlertidigAdresse.getAdressetype().equals("gateadresse")
                    && kontaktadresse.getVegadresse() != null) {
                var gateadresse = (Adresse.Gateadresse) midlertidigAdresse.getStrukturertAdresse();
                var vegadresse = kontaktadresse.getVegadresse();
                if (!gateadresse.gatenavn.equalsIgnoreCase(vegadresse.getAdressenavn())) {
                    ulikeFelter.add("Adressenavn");
                }
                if (!gateadresse.husnummer.equalsIgnoreCase(vegadresse.getHusnummer().toString())) {
                    ulikeFelter.add("Husnummer");
                }
                if (gateadresse.husbokstav != null
                        && vegadresse.getHusbokstav() != null
                        && !gateadresse.husbokstav.equalsIgnoreCase(vegadresse.getHusbokstav())) {
                    ulikeFelter.add("Husbokstav");
                }
                if (gateadresse.bolignummer != null
                        && vegadresse.getBruksenhetsnummer() != null
                        && !gateadresse.bolignummer.equalsIgnoreCase(vegadresse.getBruksenhetsnummer())) {
                    ulikeFelter.add("Bruksenhetsnummer");
                }
                if (!gateadresse.postnummer.equalsIgnoreCase(vegadresse.getPostnummer())) {
                    ulikeFelter.add("Postnummer");
                }
                if (vegadresse.getKommunenummer() != null && !gateadresse.kommunenummer.equalsIgnoreCase(vegadresse.getKommunenummer())) {
                    ulikeFelter.add("Kommunenummer");
                }
            }
            if (midlertidigAdresse.getAdressetype() != null
                    && midlertidigAdresse.getAdressetype().equalsIgnoreCase("matrikkeladresse")) {
                log.info("MidlertidigAdresse i TPS er matrikkeladresse. Kontaktadresse i PDL finnes ikke som matrikkeladresse.");
            }

            if (midlertidigAdresse.getAdressetype() != null) {
                log.info("Midlertidig adresse - ukjent adressetype {}", midlertidigAdresse.getAdressetype());
            }

            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i MidlertidigAdresse i Person_v3 vs kontaktadresse i PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("MidlertidigAdresse i Person_v3 og kontaktadresse i PDL er like");
            }
        }
    }
}
