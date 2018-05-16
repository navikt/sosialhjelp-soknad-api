package no.nav.sbl.dialogarena.sendsoknad.domain.adresse;

import java.util.ArrayList;
import java.util.List;

public interface AdresseSokConsumer {

    AdressesokRespons sokAdresse(String adresse);


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
    }
}
