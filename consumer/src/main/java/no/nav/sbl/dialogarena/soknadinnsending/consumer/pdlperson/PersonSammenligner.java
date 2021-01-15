package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Bostedsadresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Kontaktadresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Oppholdsadresse;
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

    public void sammenlignMidlertidigAdresse(Adresse midlertidigAdresse, Oppholdsadresse oppholdsadresse) {
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
                if (!gateadresse.gatenavn.equalsIgnoreCase(oppholdsadresse.getVegadresse().getAdressenavn())) {
                    ulikeFelter.add("Adressenavn");
                }
                if (!gateadresse.husnummer.equalsIgnoreCase(oppholdsadresse.getVegadresse().getHusnummer().toString())) {
                    ulikeFelter.add("Husnummer");
                }
                if (gateadresse.husbokstav != null
                        && oppholdsadresse.getVegadresse().getHusbokstav() != null
                        && !gateadresse.husbokstav.equalsIgnoreCase(oppholdsadresse.getVegadresse().getHusbokstav())) {
                    ulikeFelter.add("Husbokstav");
                }
                if (gateadresse.bolignummer != null
                        && oppholdsadresse.getVegadresse().getBruksenhetsnummer() != null
                        && !gateadresse.bolignummer.equalsIgnoreCase(oppholdsadresse.getVegadresse().getBruksenhetsnummer())) {
                    ulikeFelter.add("Bruksenhetsnummer");
                }
                if (!gateadresse.postnummer.equalsIgnoreCase(oppholdsadresse.getVegadresse().getPostnummer())) {
                    ulikeFelter.add("Postnummer");
                }
                if (!gateadresse.kommunenummer.equalsIgnoreCase(oppholdsadresse.getVegadresse().getKommunenummer())) {
                    ulikeFelter.add("Husbokstav");
                }
            }
            if (midlertidigAdresse.getAdressetype() != null
                    && midlertidigAdresse.getAdressetype().equalsIgnoreCase("matrikkeladresse")) {
                log.info("MidlertidigAdresse i TPS er matrikkeladresse. Har ikke hentet matrikkeladresse for Oppholdsadresse i PDL.");
            }

            if (midlertidigAdresse.getAdressetype() != null) {
                log.info("Midlertidig adresse - ukjent adressetyp {}", midlertidigAdresse.getAdressetype());
            }

            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i MidlertidigAdresse i Person_v3 vs Oppholdsadresse i PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("MidlertidigAdresse i Person_v3 og Oppholdsadresse i PDL er like");
            }
        }
    }

    public void sammenlignMidlertidigAdresse(Adresse midlertidigAdresse, Kontaktadresse kontaktadresse) {
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
                if (!gateadresse.gatenavn.equalsIgnoreCase(kontaktadresse.getVegadresse().getAdressenavn())) {
                    ulikeFelter.add("Adressenavn");
                }
                if (!gateadresse.husnummer.equalsIgnoreCase(kontaktadresse.getVegadresse().getHusnummer().toString())) {
                    ulikeFelter.add("Husnummer");
                }
                if (gateadresse.husbokstav != null
                        && kontaktadresse.getVegadresse().getHusbokstav() != null
                        && !gateadresse.husbokstav.equalsIgnoreCase(kontaktadresse.getVegadresse().getHusbokstav())) {
                    ulikeFelter.add("Husbokstav");
                }
                if (gateadresse.bolignummer != null
                        && kontaktadresse.getVegadresse().getBruksenhetsnummer() != null
                        && !gateadresse.bolignummer.equalsIgnoreCase(kontaktadresse.getVegadresse().getBruksenhetsnummer())) {
                    ulikeFelter.add("Bruksenhetsnummer");
                }
                if (!gateadresse.postnummer.equalsIgnoreCase(kontaktadresse.getVegadresse().getPostnummer())) {
                    ulikeFelter.add("Postnummer");
                }
                if (kontaktadresse.getVegadresse().getKommunenummer() != null && !gateadresse.kommunenummer.equalsIgnoreCase(kontaktadresse.getVegadresse().getKommunenummer())) {
                    ulikeFelter.add("Kommunenummer");
                }
            }
            if (midlertidigAdresse.getAdressetype() != null
                    && midlertidigAdresse.getAdressetype().equalsIgnoreCase("matrikkeladresse")) {
                log.info("MidlertidigAdresse i TPS er matrikkeladresse. Kontaktadresse i PDL er ikke mulig som matrikkeladresse.");
            }

            if (midlertidigAdresse.getAdressetype() != null) {
                log.info("Midlertidig adresse - ukjent adressetyp {}", midlertidigAdresse.getAdressetype());
            }

            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i MidlertidigAdresse i Person_v3 vs kontaktadresse i PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("MidlertidigAdresse i Person_v3 og kontaktadresse i PDL er like");
            }
        }
    }
}
