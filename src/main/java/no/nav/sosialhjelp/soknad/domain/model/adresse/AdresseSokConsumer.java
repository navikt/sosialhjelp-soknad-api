//package no.nav.sosialhjelp.soknad.domain.model.adresse;
//
//import no.nav.sosialhjelp.soknad.business.service.adressesok.Sokedata;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public interface AdresseSokConsumer {
//
//    AdressesokRespons sokAdresse(Sokedata adressefelter);
//    void ping();
//
//    class AdressesokRespons {
//        public boolean flereTreff;
//        public List<AdresseData> adresseDataList = new ArrayList<>();
//    }
//
//    class AdresseData {
//        public String kommunenummer;
//        public String kommunenavn;
//        public String adressenavn;
//        public String husnummerFra;
//        public String husnummerTil;
//        public String postnummer;
//        public String poststed;
//        public String geografiskTilknytning;
//        public String gatekode;
//        public String bydel;
//
//        public String husnummer;
//        public String husbokstav;
//
//        @Override
//        public String toString() {
//            return adressenavn + ", " + postnummer + " " + poststed;
//        }
//    }
//}
