//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel;
//
//import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
//import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelFordeling.Husnummerfordeling;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//import static java.util.Objects.isNull;
//
//@Component
//public class BydelService {
//
//    public static final String BYDEL_MARKA = "030117";
//
//    private final List<BydelFordeling> markaBydelFordeling;
//
//    public BydelService(List<BydelFordeling> markaBydelFordeling) {
//        this.markaBydelFordeling = markaBydelFordeling;
//    }
//
//    public String getBydelTilForMarka(AdresseForslag adresseForslag) {
//        return markaBydelFordeling.stream()
//                .filter(f -> f.getVeiadresse().trim().equalsIgnoreCase(adresseForslag.adresse.trim()))
//                .filter(f -> isInHusnummerfordeling(f.getHusnummerfordeling(), adresseForslag.husnummer))
//                .findFirst()
//                .map(BydelFordeling::getBydelTil)
//                .orElse(adresseForslag.geografiskTilknytning);
//    }
//
//    private boolean isInHusnummerfordeling(List<Husnummerfordeling> husnummerfordeling, String husnummer) {
//        return husnummerfordeling.stream().anyMatch(m -> isInRangeHusnummer(m, husnummer));
//    }
//
//    private boolean isInRangeHusnummer(Husnummerfordeling husnummerfordeling, String husnummer) {
//        if (isNull(husnummer) || !StringUtils.isNumeric(husnummer)) {
//            return false;
//        }
//
//        var intHusnummer = Integer.parseInt(husnummer.trim());
//        var fra = husnummerfordeling.getFra();
//        var til = husnummerfordeling.getTil();
//
//        var isEven = intHusnummer % 2 == 0;
//
//        if (husnummerfordeling.getType() == BydelFordeling.HusnummerfordelingType.ALL) {
//            return true;
//        } else if (husnummerfordeling.getType() == BydelFordeling.HusnummerfordelingType.EVEN) {
//            return isEven && intHusnummer >= fra && intHusnummer <= til;
//        } else if (husnummerfordeling.getType() == BydelFordeling.HusnummerfordelingType.ODD) {
//            return !isEven && intHusnummer >= fra && intHusnummer <= til;
//        } else {
//            return false;
//        }
//    }
//}
