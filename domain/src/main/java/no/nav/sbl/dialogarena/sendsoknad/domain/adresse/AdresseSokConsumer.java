package no.nav.sbl.dialogarena.sendsoknad.domain.adresse;

import java.util.ArrayList;
import java.util.List;

public interface AdresseSokConsumer {

    AdressesokRespons sokAdresse(String adresse);
    AdressesokRespons sokAdresse(Sokedata adressefelter);


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
    
    class Sokedata {
        public String adresse;
        public String husnummer;
        public String husbokstav;
        public String postnummer;
        public String poststed;
        
        public String kommunenummer;
        
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
    }
}
