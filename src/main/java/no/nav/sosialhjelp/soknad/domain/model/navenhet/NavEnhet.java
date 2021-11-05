//package no.nav.sosialhjelp.soknad.domain.model.navenhet;
//
//import java.util.Objects;
//
//public class NavEnhet {
//    public String enhetNr;
//    public String navn;
//    public String kommunenavn;
//    public String sosialOrgnr;
//
//    @Override
//    public boolean equals(Object other) {
//        if (this == other) return true;
//        if (other == null || getClass() != other.getClass()) return false;
//        NavEnhet navEnhet = (NavEnhet) other;
//        return Objects.equals(enhetNr, navEnhet.enhetNr) &&
//                Objects.equals(navn, navEnhet.navn) &&
//                Objects.equals(sosialOrgnr, navEnhet.sosialOrgnr);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(enhetNr, navn, sosialOrgnr);
//    }
//}
