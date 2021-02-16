package no.nav.sosialhjelp.soknad.mock.adresse;

import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Sokedata;

import java.util.Arrays;
import java.util.List;

import static no.nav.sosialhjelp.soknad.domain.model.adresse.AdresseSokConsumer.Soketype.FONETISK;

class AdresseSokMockHelper {

    static final Sokedata oslo = new Sokedata().withAdresse("Oslo").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata osloLowercase = new Sokedata().withAdresse("oslo").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata bergen = new Sokedata().withAdresse("Bergen").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata bergenLowercase = new Sokedata().withAdresse("bergen").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata trondheim = new Sokedata().withAdresse("Trondheim").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata trondheimLowercase = new Sokedata().withAdresse("trondheim").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata tromso = new Sokedata().withAdresse("Tromsø").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata tromsoLowercase = new Sokedata().withAdresse("tromsø").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata kristiansand = new Sokedata().withAdresse("Kristiansand").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata kristiansandLowercase = new Sokedata().withAdresse("kristiansand").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata svalbard = new Sokedata().withAdresse("Svalbard").withHusnummer("").withSoketype(FONETISK);
    static final Sokedata svalbardLowercase = new Sokedata().withAdresse("svalbard").withHusnummer("").withSoketype(FONETISK);

    private static final AdresseData a1 = createAdresseData("1201", "Bergen", "SANNERGATA", "0001", "0010", "1337", "Leet", "120102", "02081", "120102");
    private static final AdresseData a2 = createAdresseData("1201", "Bergen", "SANNERGATA", "0011", "9999", "1337", "Leet", "120107", "02081", "120107");
    private static final AdresseData a3 = createAdresseData("0301", "Oslo", "Økernveien", "94", "0579", "Oslo", "030105", "03011", "030105");
    private static final AdresseData a4 = createAdresseData("0301", "Oslo", "Slottsplassen", "1", "0110", "Oslo", "030101", "03012", "030101");
    private static final AdresseData a5 = createAdresseData("1601", "Trondheim", "Bispegata", "14", "7012", "Trondheim", "160101", "16011", "160101");
    private static final AdresseData a6 = createAdresseData("0301", "Oslo", "Problemveien", "9", "0313", "Oslo", "030103", "03013", "030103");
    private static final AdresseData a7 = createAdresseData("0301", "Oslo", "Holmenkollen", "1", "0112", "Oslo", "030101", "03014", "030101");
    private static final AdresseData a8 = createAdresseData("0301", "Oslo", "Grefsenkollveien", "100", "0490", "Oslo", "030104", "03015", "030104");
    private static final AdresseData a9 = createAdresseData("0301", "Oslo", "Sørengkaia", "1", "0194", "Oslo", "030101", "03016", "030101");
    private static final AdresseData a10 = createAdresseData("0301", "Oslo", "Olaf Ryes plass", "1", "0552", "Oslo", "030105", "03017", "030105");
    private static final AdresseData a11 = createAdresseData("1001", "Kristiansand", "Kardemomme By", "4609", "4636", "Kristiansand", "", "10011", "1001");
    private static final AdresseData a12 = createAdresseData("2100", "Svalbard", "Vei 505", "8", "9170", "Svalbard", "2100", "21001", "2100");
    private static final AdresseData a13 = createAdresseData("1902", "Tromsø", "Hans Nilsens vei", "41", "9020", "Tromsdalen", "1902", "19021", "1902");
    private static final AdresseData a14 = createAdresseData("1201", "Bergen", "Bryggen", "1", "1337", "Oslo", "120101", "12012", "120101");
    private static final AdresseData a15 = createAdresseData("1201", "Bergen", "Fløyen", "1", "1337", "Oslo", "120104", "12013", "120104");

    static final List<AdresseData> adresseDataList = Arrays.asList(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15);

    private AdresseSokMockHelper() {
        //no-op
    }

    private static AdresseData createAdresseData(String kommunenummer, String kommunenavn, String adressenavn, String husnummer, String postnummer, String poststed, String geografiskTilknytning, String gatekode, String bydel) {
        final AdresseData baseAdresseData = createBaseAdresseData(kommunenummer, kommunenavn, adressenavn, postnummer, poststed, geografiskTilknytning, gatekode, bydel);
        baseAdresseData.husnummer = husnummer;
        return baseAdresseData;
    }

    private static AdresseData createAdresseData(String kommunenummer, String kommunenavn, String adressenavn, String husnummerFra, String husnummerTil, String postnummer, String poststed, String geografiskTilknytning, String gatekode, String bydel) {
        final AdresseData baseAdresseData = createBaseAdresseData(kommunenummer, kommunenavn, adressenavn, postnummer, poststed, geografiskTilknytning, gatekode, bydel);
        baseAdresseData.husnummerFra = husnummerFra;
        baseAdresseData.husnummerTil = husnummerTil;
        return baseAdresseData;
    }

    private static AdresseData createBaseAdresseData(String kommunenummer, String kommunenavn, String adressenavn, String postnummer, String poststed, String geografiskTilknytning, String gatekode, String bydel) {
        final AdresseData ad = new AdresseData();
        ad.kommunenummer = kommunenummer;
        ad.kommunenavn = kommunenavn;
        ad.adressenavn = adressenavn;
        ad.postnummer = postnummer;
        ad.poststed = poststed;
        ad.geografiskTilknytning = geografiskTilknytning;
        ad.gatekode = gatekode;
        ad.bydel = bydel;
        return ad;
    }
}
