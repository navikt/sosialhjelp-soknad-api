package no.nav.sbl.dialogarena.sendsoknad.domain.adresse;

import java.util.ArrayList;
import java.util.List;

public interface AdresseSokConsumer {

    AdressesokRespons sokAdresse(String adresse);
    AdressesokRespons sokAdresse(Sokedata adressefelter);
    void ping();

    class AdressesokRespons {
        public boolean flereTreff;
        public List<AdresseData> adresseDataList = new ArrayList<>();
    }

    class AdresseData {
        public String kommunenummer;
        public String kommunenavn;
        public String adressenavn;
        public String husnummerFra;
        public String husnummerTil;
        public String postnummer;
        public String poststed;
        public String geografiskTilknytning;
        public String gatekode;
        public String bydel;
        
        public String husnummer;
        public String husbokstav;
        
        @Override
        public String toString() {
            return adressenavn + ", " + postnummer + " " + poststed;
        }
    }
    
    enum Soketype {
        FONETISK("F"),
        EKSAKT("E"),
        TILFELDIG("T"),
        LIGNENDE("L");

        String tpsKode;
        
        Soketype(String tpsKode) {
            this.tpsKode = tpsKode;
        }
        
        public String toTpsKode() {
            return tpsKode;
        }
    }
    
    class Sokedata {
        public String adresse;
        public String husnummer;
        public String husbokstav;
        public String postnummer;
        public String poststed;
        
        public String kommunenummer;
        public Soketype soketype = Soketype.LIGNENDE;
        
        public Sokedata withAdresse(String adresse) {
            this.adresse = adresse;
            return this;
        }
        
        public Sokedata withHusnummer(String husnummer) {
            this.husnummer = husnummer;
            return this;
        }
        
        public Sokedata withHusbokstav(String husbokstav) {
            this.husbokstav = husbokstav;
            return this;
        }
        
        public Sokedata withPostnummer(String postnummer) {
            this.postnummer = postnummer;
            return this;
        }
        
        public Sokedata withPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }
        
        public Sokedata withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }
        
        public Sokedata withSoketype(Soketype soketype) {
            if (soketype == null) {
                throw new IllegalArgumentException("soketype == null");
            }
            this.soketype = soketype;
            return this;
        }

        @Override
        public String toString() {
            return "Sokedata [adresse=" + adresse + ", husnummer=" + husnummer + ", husbokstav=" + husbokstav
                    + ", postnummer=" + postnummer + ", poststed=" + poststed + ", kommunenummer=" + kommunenummer
                    + ", soketype=" + soketype + "]";
        }
    }
}
