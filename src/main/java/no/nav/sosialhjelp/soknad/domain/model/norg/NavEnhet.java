package no.nav.sosialhjelp.soknad.domain.model.norg;

import java.util.List;
import java.util.Objects;

public class NavEnhet {
    public String enhetNr;
    public String navn;
    public String kommunenavn;
    public String sosialOrgnr;

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        NavEnhet navEnhet = (NavEnhet) other;
        return Objects.equals(enhetNr, navEnhet.enhetNr) &&
                Objects.equals(navn, navEnhet.navn) &&
                Objects.equals(sosialOrgnr, navEnhet.sosialOrgnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enhetNr, navn, sosialOrgnr);
    }

    public static class Kontaktinformasjon {

        public Adresse postdresse;
        public List<Publikumsmottak> publikumsmottak;
        public Telefon telefon;

        public static class Adresse {
            public String type;
            public String postnummer;
            public String poststed;

            public String postboksanlegg;
            public String postboksnummer;

            public String gatenavn;
            public String husnummer;
            public String husbokstav;
            public String adresseTilleggsnavn;

            public static Adresse postboks(String postnummer, String poststed, String postboksanlegg, String postboksnummer) {
                Adresse adresse = new Adresse();
                adresse.type = "postboksadresse";
                adresse.postnummer = postnummer;
                adresse.poststed = poststed;
                adresse.postboksanlegg = postboksanlegg;
                adresse.postboksnummer = postboksnummer;
                return adresse;
            }

            public static Adresse gateadresse(String postnummer, String poststed, String gatenavn,
                                              String husnummer, String husbokstav, String adresseTilleggsnavn) {
                Adresse adresse = new Adresse();
                adresse.type = "stedsadresse";
                adresse.postnummer = postnummer;
                adresse.poststed = poststed;
                adresse.gatenavn = gatenavn;
                adresse.husnummer = husnummer;
                adresse.husbokstav = husbokstav;
                adresse.adresseTilleggsnavn = adresseTilleggsnavn;
                return adresse;
            }
        }

        public static class Publikumsmottak {
            public Adresse besoksadresse;
            public List<Apningstid> apningstider;
        }

        public static class Apningstid {
            public String dag;
            public String fra;
            public String til;
            public String kommentar;
            public boolean stengt;
        }

        public static class Telefon {
            public String telefonnr;
            public String telefonKommentar;

            public static Telefon telefon(String telefonnr, String telefonKommentar) {
                Telefon telefon = new Telefon();
                telefon.telefonnr = telefonnr;
                telefon.telefonKommentar = telefonKommentar;
                return telefon;
            }
        }
    }
}
