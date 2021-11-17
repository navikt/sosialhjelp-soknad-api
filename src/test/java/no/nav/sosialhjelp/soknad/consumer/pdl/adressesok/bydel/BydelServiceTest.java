//package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel;
//
//import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
//import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelFordeling.Husnummerfordeling;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static java.util.Arrays.asList;
//import static java.util.Collections.singletonList;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelFordeling.HusnummerfordelingType.ALL;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelFordeling.HusnummerfordelingType.EVEN;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelFordeling.HusnummerfordelingType.ODD;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class BydelServiceTest {
//
//    private static final String TESTVEIEN = "testveien";
//    private static final String TESTGATEN = "testgaten";
//
//    private static final String BYDEL_GRORUD = "030110";
//    private static final String BYDEL_VESTRE_AKER = "030107";
//    private static final String BYDEL_NORDRE_AKER = "030108";
//
//    private final BydelService bydelService = new BydelService(markaBydelFordeling());
//
//    @Test
//    void skalReturnereBydelTil() {
//        var testveien14 = createAdresseForslag(TESTVEIEN, "14");
//        assertThat(bydelService.getBydelTilForMarka(testveien14)).isEqualTo(BYDEL_GRORUD);
//
//        var testgaten1 = createAdresseForslag(TESTGATEN, "1");
//        var testgaten100 = createAdresseForslag(TESTGATEN, "100");
//
//        assertThat(bydelService.getBydelTilForMarka(testgaten1)).isEqualTo(BYDEL_VESTRE_AKER);
//        assertThat(bydelService.getBydelTilForMarka(testgaten100)).isEqualTo(BYDEL_VESTRE_AKER);
//
//        var testgaten101 = createAdresseForslag(TESTGATEN, "101");
//        var testgaten899 = createAdresseForslag(TESTGATEN, "899");
//
//        assertThat(bydelService.getBydelTilForMarka(testgaten101)).isEqualTo(BYDEL_NORDRE_AKER);
//        assertThat(bydelService.getBydelTilForMarka(testgaten899)).isEqualTo(BYDEL_NORDRE_AKER);
//    }
//
//    @Test
//    void skalReturnereAdresseforslagGeografiskTilknytningHvisBydelFordelingIkkeFinnes() {
//        var adresseForslag = createAdresseForslag("annen adresse", "14");
//        var bydelTil = bydelService.getBydelTilForMarka(adresseForslag);
//
//        assertThat(bydelTil).isEqualTo(BYDEL_MARKA);
//    }
//
//    private AdresseForslag createAdresseForslag(String adresse, String husnummer) {
//        var af = new AdresseForslag();
//        af.adresse = adresse;
//        af.husnummer = husnummer;
//        af.geografiskTilknytning = BYDEL_MARKA;
//        return af;
//    }
//
//    private List<BydelFordeling> markaBydelFordeling() {
//        return asList(
//                new BydelFordeling(TESTVEIEN, "gatekode", singletonList(new Husnummerfordeling(1, 9999, ALL)), BYDEL_MARKA, BYDEL_GRORUD, "Grorud"),
//                new BydelFordeling(TESTGATEN, "gatekode", asList(new Husnummerfordeling(1, 99, ODD), new Husnummerfordeling(2, 100, EVEN)), BYDEL_MARKA, BYDEL_VESTRE_AKER, "Vestre Aker"),
//                new BydelFordeling(TESTGATEN, "gatekode", asList(new Husnummerfordeling(101, 9999, ODD), new Husnummerfordeling(102, 9999, EVEN)), BYDEL_MARKA, BYDEL_NORDRE_AKER, "Nordre Aker")
//        );
//    }
//}