package no.nav.sosialhjelp.soknad.domain.model.util;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;


public final class KommuneTilNavEnhetMapper {

    private KommuneTilNavEnhetMapper() {
    }

    public static final Map<String, String> IKS_KOMMUNER = new ImmutableMap.Builder<String, String>()
            .put("3438", "Nord-Fron") // Sør-Fron (nytt kommunenummer)
            .put("3439", "Nord-Fron") // Ringebu (nytt kommunenummer)
            .put("3050", "Rollag")    // Flesberg (nytt kommunenummer)
            .put("3052", "Rollag")    // Nore og Uvdal (nytt kommunenummer)
            .put("1151", "Haugesund") // Utsira (samme kommunenummer etter 2020)
            .put("4207", "Kvinesdal") // Flekkefjord
            .put("4225", "Kvinesdal") // Lyngdal
            .put("4206", "Kvinesdal") // Farsund
            .put("4228", "Kvinesdal") // Sirdal
            .put("4218", "Vennesla")  // Iveland
            .put("4224", "Vennesla")  // Åseral
            .put("4221", "Vennesla")  // Valle
            .put("4222", "Vennesla")  // Bykle
            .put("4220", "Vennesla")  // Bygland
            .put("5032", "Stjørdal")  // Selbu
            .put("5033", "Stjørdal")  // Tydal
            .put("5034", "Stjørdal")  // Meråker
            .put("5036", "Stjørdal")  // Frosta
            .put("1853", "Narvik")    // Evenes
            .put("5414", "Narvik")    // Gratangen
            .put("3822", "Tokke")     // Nissedal
            .put("3821", "Tokke")     // Kvitseid
            .put("3825", "Tokke")     // Vinje
            .put("3823", "Tokke")     // Fyresdal
            .put("3820", "Tokke")     // Seljord
            .put("3042", "Gol")       // Hemsedal
            .put("3039", "Gol")       // Flå
            .put("3040", "Gol")       // Nesbyen
            .put("3043", "Gol")       // Ål
            .put("3044", "Gol")       // Hol
            .put("5058", "Osen")      // Åfjord
            .put("1121", "Klepp")     // Time
            .build();

    private static final Map<String, String> PROD_ORGANISASJONSNUMMER = new ImmutableMap.Builder<String, String>()
            .put("0701", "974605171")    // Horten
            .put("1247", "974600889")    // Askøy
            .put("0806", "995509970")    // Skien
            .put("0326", "970534644")    // Alna, Oslo kommune
            .put("0330", "974778874")    // Bjerke, Oslo kommune
            .put("0312", "874778702")    // Frogner, Oslo kommune
            .put("0316", "974778742")    // Gamle Oslo, Oslo kommune
            .put("0328", "974778866")    // Grorud, Oslo kommune
            .put("0315", "870534612")    // Grünerløkka, Oslo kommune
            .put("0331", "974778882")    // Nordre Aker, Oslo kommune
            .put("0318", "970534679")    // Nordstrand, Oslo kommune
            .put("0314", "974778726")    // Sagene, Oslo kommune
            .put("0313", "971179686")    // St. Hanshaugen, Oslo kommune
            .put("0327", "874778842")    // Stovner, Oslo kommune
            .put("0319", "972408875")    // Søndre Nordstrand, Oslo kommune
            .put("0335", "971022051")    // Ullern, Oslo kommune
            .put("0334", "970145311")    // Vestre Aker, Oslo kommune
            .put("0321", "974778807")    // Østensjø, Oslo kommune
            .put("5701", "892284792")    // Falkenborg, Trondheim kommune
            .put("5702", "992284838")    // Lerkendal, Trondheim kommune & Klæbu
            .put("1161", "873864192")    // Eiganes og Tasta, Stavanger kommune
            .put("1164", "976670531")    // Hillevåg og Hinna, Stavanger kommune
            .put("1162", "973864181")    // Hundvåg og Storhaug, Stavanger kommune
            .put("1165", "973864203")    // Madla, Stavanger kommune
            .put("1169", "874617512")    // Rennesøy og Finnøy, Stavanger kommune
            .put("1102", "874610712")    // Sandnes
            .put("1119", "976827961")    // Hå
            .put("1124", "948243113")    // Sola
            .put("5303", "997784618")    // Larvik
            .put("0403", "974623811")    // Hamar
            .put("1122", "974616564")    // Gjesdal
            .put("0529", "986838961")    // Vestre Toten
            .put("0626", "974545861")    // Lier
            .put("1866", "874052582")    // Hadsel
            .put("1133", "974549751")    // Hjelmeland
            .put("1135", "974617250")    // Sauda
            .put("1130", "974616734")    // Strand
            .put("0624", "994952854")    // Øvre Eiker
            .put("1134", "974616998")    // Suldal
            .put("0532", "974596016")    // Jevnaker
            .put("1401", "974551918")    // Kinn
            .put("1238", "944233199")    // Kvam
            .put("1933", "921858361")    // Balsfjord og Storfjord
            .put("0221", "976637216")    // Aurskog-Høland
            .put("1224", "993975192")    // Kvinnherad
            .put("1824", "876834162")    // Vefsn
            .put("0815", "979525095")    // Kragerø
            .put("0516", "974543303")    // Nord-Fron, Sør-Fron, Ringebu
            .put("2004", "974601753")    // Hammerfest
            .put("1127", "988052310")    // Randaberg-Kvitsøy
            .put("0412", "976639618")    // Ringsaker
            .put("0426", "974550342")    // Våler (Hedmark)
            .put("1037", "964964076")    // Kvinesdal, Flekkefjord, Lyngdal, Farsund, Sirdal
            .put("0106", "993393851")    // Fredrikstad
            .put("0605", "976820835")    // Ringerike
            .put("0418", "964950768")    // Nord-Odal
            .put("1235", "973951270")    // Voss
            .put("2003", "974622238")    // Vadsø
            .put("1243", "992179457")    // Bjørnafjorden
            .put("0229", "974604175")    // Enebakk
            .put("0517", "974562294")    // Sel
            .put("0513", "976641175")    // Lom-Skjåk
            .put("1219", "834210622")    // Bømlo
            .put("0515", "976641310")    // Vågå
            .put("0502", "974567776")    // Gjøvik
            .put("0906", "976825950")    // Arendal
            .put("1903", "978608418")    // Harstad
            .put("0511", "919059303")    // Lesja-Dovre
            .put("1804", "983942962")    // Bodø
            .put("1913", "959469326")    // Skånland, Tjelsund
            .put("0417", "994743767")    // Stange
            .put("1702", "983921000")    // Steinkjer, Snåase-Snåsa, Inderøy
            .put("0211", "974574306")    // Vestby
            .put("1222", "974575728")    // Fitjar
            .put("1517", "978607063")    // Hareid
            .put("1516", "978607063")    // Ulstein
            .put("1514", "978607063")    // Sande
            .put("0105", "974560593")    // Sarpsborg
            .put("1221", "991459634")    // Stord
            .put("0827", "973802003")    // Hjartdal
            .put("0807", "984001797")    // Notodden
            .put("0632", "964963282")    // Rollag, Flesberg, Nore og Uvdal
            .put("0604", "974572486")    // Kongsberg
            .put("0826", "874548472")    // Tinn
            .put("0427", "976640322")    // Elverum
            .put("0237", "974604442")    // Eidsvoll
            .put("0239", "939780777")    // Hurdal
            .put("0423", "964948143")    // Grue
            .put("1548", "974545284")    // Hustadvika
            .put("0128", "997220838")    // Rakkestad
            .put("0236", "976665589")    // Nes (Akershus)
            .put("0234", "974547678")    // Gjerdrum
            .put("0101", "959159092")    // Halden
            .put("0235", "983870953")    // Ullensaker
            .put("0238", "874604682")    // Nannestad
            .put("1833", "993576190")    // Rana
            .put("1149", "995075199")    // Karmøy,Bokn
            .put("1515", "983931073")    // Herøy, Vanylven
            .put("1106", "974575906")    // Haugesund, Utsira
            .put("1663", "974624257")    // Malvik
            .put("1146", "974617757")    // Tysvær
            .put("0536", "974596393")    // Søndre Land
            .put("0219", "974702401")    // Bærum
            .put("1416", "976831322")    // Høyanger
            .put("0402", "944117784")    // Kongsvinger
            .put("0419", "874602612")    // Sør-Odal
            .put("0215", "974600544")    // Frogn
            .put("1719", "974556464")    // Levanger
            .put("0230", "976637992")    // Lørenskog
            .put("0420", "874602752")    // Eidskog
            .put("1438", "973981749")    // Bremanger
            .put("1443", "974556154")    // Stad
            .put("1445", "976675185")    // Gloppen
            .put("1449", "976831772")    // Stryn
            .put("1505", "974775719")    // Kristiansund
            .put("1554", "962378064")    // Averøy
            .put("1557", "974782170")    // Gjemnes
            .put("1942", "974574101")    // Nordreisa
            .put("1941", "976835441")    // Skjervøy
            .put("0425", "974602970")    // Åsnes
            .put("0135", "974569906")    // Råde
            .put("1002", "921060440")    // Nye Lindesnes
            .put("5302", "974607395")    // Færder
            .put("0704", "974586002")    // Tønsberg
            .put("5301", "974542811")    // Holmestrand
            .put("1504", "975914895")    // Ålesund, Fjord
            .put("1532", "974619148")    // Giske
            .put("1923", "974597578")    // Salangen, Lavangen og Dyrøy
            .put("0542", "994057790")    // Nord-Aurdal, Sør-Aurdal, Etnedal, Vestre Slidre, Øystre Slidre, Vang
            .put("0538", "974546302")    // Nordre Land
            .put("0817", "974551063")    // Drangedal
            .put("1621", "974620529")    // Ørland
            .put("0231", "974555379")    // Lillestrøm
            .put("2025", "987590254")    // Deatnu/Tana
            .put("0213", "974593408")    // Nordre Follo
            .put("1246", "974586312")    // Øygarden
            .put("1014", "936846777")    // Bykle, Bygland, Valle, Åseral, Vennesla og Iveland
            .put("0710", "974594188")    // Sandefjord
            .put("1560", "916359616")    // Tingvoll
            .put("1566", "974619962")    // Surnadal
            .put("0911", "964964998")    // Gjerstad
            .put("1563", "813112892")    // Sunndal
            .put("1432", "976831683")    // Sunnfjord
            .put("5703", "874577502")    // Indre Fosen
            .put("1573", "976832418")    // Smøla
            .put("1576", "921358288")    // Aure
            .put("1783", "918964118")    // Selbu, Tydal, Meråker, Stjørdal og Frosta
            .put("0228", "976637437")    // Rælingen
            .put("1805", "974592630")    // Narvik, Evenes og Gratangen
            .put("1931", "974562820")    // Senja
            .put("0833", "974610175")    // Nissedal, Kvitseid, Tokke, Vinje, Fyresdal og Seljord
            .put("0528", "974604264")    // Østre Toten
            .put("1870", "974545713")    // Sortland
            .put("0914", "964965781")    // Tvedestrand
            .put("0912", "964965048")    // Vegårshei
            .put("0617", "974607948")    // Gol, Hemsedal, Flå, Nesbyen, Ål og Hol
            .put("1630", "974578069")    // Osen og Åfjord
            .put("1620", "874560332")    // Hitra og Frøya
            .put("2012", "974580713")    // Alta
            .put("0124", "998432189")    // Indre Østfold
            .put("0919", "946439045")    // Froland
            .put("0929", "864965962")    // Åmli
            .put("1502", "986852522")    // Molde
            .put("1120", "974549611")    // Klepp og Time
            .put("0901", "964977402")    // Risør
            .put("0904", "994161105")    // Grimstad
            .put("1940", "976979958")    // Gáivuotna/Kåfjord

            .build();

    private static final Map<String, String> TEST_ORGANISASJONSNUMMER = new ImmutableMap.Builder<String, String>()
            .put("0701", "910940066")   // Horten
            .put("1247", "910230182")   // Askøy

            .put("0312", "910229699")   // Frogner,             Oslo kommune
            .put("0315", "811213322")   // Grünerløkka,         Oslo kommune - OBS: Brukes i mock!!!
            .put("0328", "910229702")   // Grorud,              Oslo kommune
            .put("0327", "910589792")   // Stovner,             Oslo kommune
            .put("0314", "910565338")   // Sagene,              Oslo kommune
            .put("0318", "910309935")   // Nordstrand,          Oslo kommune
            .put("0319", "910723499")   // Søndre Nordstrand,   Oslo kommune
            .put("0316", "910229567")   // Gamle Oslo,          Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0313", "910229567")   // St. Hanshaugen,      Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0335", "910229567")   // Ullern,              Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0334", "910229567")   // Vestre Aker,         Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0331", "910229567")   // Nordre Aker,         Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0330", "910229567")   // Bjerke,              Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0326", "910229567")   // Alna,                Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0321", "910229567")   // Østensjø,            Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!

            .put("0219", "910231065")   // Bærum
            .put("5701", "910230646")   // NAV Falkenborg, Trondheim kommune
            .put("5702", "910230611")   // NAV Lerkendal, Trondheim kommunne
            .put("1161", "910229567")   // Eiganes og Tasta, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("1164", "910229567")   // Hillevåg og Hinna, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("1162", "910229567")   // Hundvåg og Storhaug, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("1165", "910229567")   // Madla, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0403", "910229567")   // Hamar - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0516", "910229567")   // Nord-Fron - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0519", "910229567")   // Sør-Fron - OBS: Sendes til vårt orgnummer i FIKS!
            .put("0520", "910229567")   // Ringebu - OBS: Sendes til vårt orgnummer i FIKS!
            .put("1517", "910229567")   // NAV Hareid - Ulstein - Sande - OBS: Sendes til vårt orgnummer i FIKS!

            .build();

    public static String getOrganisasjonsnummer(String enhetNr) {
        return isNonProduction() ? TEST_ORGANISASJONSNUMMER.get(enhetNr) : PROD_ORGANISASJONSNUMMER.get(enhetNr);
    }

    private static final List<String> TEST_DIGISOS_KOMMUNER = Collections.unmodifiableList(
            asList(
                    "3801", // Nytt Horten
                    "0701", // Gammel Horten (1988-2020)
                    "0703", // Gammel Horten (1858-1988)
                    "0717", // Gammel Horten (1838-1858) (Borre)
                    "0301", // Oslo
                    "1201", // Mock-data Oslo
                    "4627", // Nytt Askøy
                    "3024", // Nytt Bærum
                    "5001", // Trondheim
                    "1103", // Stavanger
                    "3403", // Nytt Hamar
                    "3436", // Nytt Nord-Fron
                    "3438", // Nytt Sør-Fron
                    "3439", // Nytt Ringebu
                    "1514"  // Nytt Sande i Møre og Romsdal
            ));

    private static final List<String> PROD_DIGISOS_KOMMUNER = Collections.unmodifiableList(
            asList(
                    "0301",
                    "5001",
                    "1103",
                    "1119",
                    "1124",
                    "1122",
                    "1866",
                    "1133",
                    "1135",
                    "1130",
                    "1134",
                    "1824",
                    "1127",
                    "1144",
                    "4624",
                    "1804",
                    "5041",
                    "5053",
                    "1517",
                    "1516",
                    "1514",
                    "1833",
                    "1149",
                    "1145",
                    "1515",
                    "1511",
                    "1106",
                    "1151",
                    "5031",
                    "1146",
                    "5037",
                    "1505",
                    "1554",
                    "1557",
                    "5406",
                    "4205",
                    "1579", // Hustadvika
                    "4649", // Stad
                    "4602", // Kinn
                    "3801", // Nytt Horten
                    "4627", // Nytt Askøy
                    "3807", // Nytt Skien
                    "1108", // Nytt Sandnes
                    "3805", // Nytt Larvik
                    "3403", // Nytt Hamar
                    "3443", // Nytt Vestre Toten
                    "3049", // Nytt Lier
                    "3048", // Nytt Øvre Eiker
                    "3053", // Nytt Jevnaker
                    "4622", // Nytt Kvam
                    "5422", // Nytt Balsfjord
                    "5425", // Nytt Storfjord
                    "3026", // Nytt Aurskog-Høland
                    "4617", // Nytt Kvinnherad
                    "3814", // Nytt Kragerø
                    "3436", // Nytt Nord-Fron
                    "3438", // Nytt Sør-Fron
                    "3439", // Nytt Ringebu
                    "3411", // Nytt Ringsaker
                    "3419", // Nytt Våler i innlandet
                    "4227", // Nytt Kvinesdal
                    "3004", // Nytt Fredrikstad
                    "3007", // Nytt Ringerike
                    "3414", // Nytt Nord-Odal
                    "4621", // Nytt Voss
                    "5405", // Nytt Vadsø
                    "3028", // Nytt Enebakk
                    "3434", // Nytt Lom
                    "3437", // Nytt Sel
                    "3433", // Nytt Skjåk
                    "4613", // Nytt Bømlo
                    "3435", // Nytt Vågå
                    "3407", // Nytt Gjøvik
                    "4203", // Nytt Arendal
                    "5402", // Nytt Harstad
                    "3431", // Nytt Dovre
                    "3432", // Nytt Lesja
                    "5412", // Nytt Tjeldsund
                    "3413", // Nytt Stange
                    "5006", // Nytt Steinkjer
                    "3019", // Nytt Vestby
                    "4615", // Nytt Fitjar
                    "3003", // Nytt Sarpsborg
                    "4614", // Nytt Stord
                    "3819", // Nytt Hjartdal
                    "3808", // Nytt Notodden
                    "3051", // Nytt Rollag
                    "3050", // Nytt Flesberg
                    "3052", // Nytt Nore og Uvdal
                    "3006", // Nytt Kongsberg
                    "3818", // Nytt Tinn
                    "3420", // Nytt Elverum
                    "3035", // Nytt Eidsvoll
                    "3037", // Nytt Hurdal
                    "3417", // Nytt Grue
                    "3016", // Nytt Rakkestad
                    "3034", // Nytt Nes (Akershus)
                    "3032", // Nytt Gjerdrum
                    "3001", // Nytt Halden
                    "3033", // Nytt Ullensaker
                    "3036", // Nytt Nannestad
                    "3447", // Nytt Søndre Land
                    "3024", // Nytt Bærum
                    "4638", // Nytt Høyanger
                    "3401", // Nytt Kongsvinger
                    "3415", // Nytt Sør-Odal
                    "3022", // Nytt Frogn
                    "3029", // Nytt Lørenskog
                    "3416", // Nytt Eidskog
                    "4648", // Nytt Bremager
                    "4650", // Nytt Gloppen
                    "4651", // Nytt Stryn
                    "5428", // Nytt Nordreisa
                    "5427", // Nytt Skjervøy
                    "3418", // Nytt Åsnes
                    "3017", // Nytt Råde
                    "3811", // Færder
                    "4207", // Flekkefjord
                    "4225", // Lyngdal
                    "4206", // Farsund
                    "4228", // Sirdal
                    "3803", // Tønsberg
                    "3802", // Holmestrand
                    "1507", // Ålesund
                    "1578", // Fjord
                    "1532", // Giske
                    "5417", // Salangen
                    "5415", // Lavangen
                    "5420", // Dyrøy
                    "3451", // Nord-Aurdal
                    "3449", // Sør-Aurdal
                    "3450", // Etnedal
                    "3452", // Vestre Slidre
                    "3453", // Øystre Slidre
                    "3454", // Vang
                    "3448", // Nordre Land
                    "3815", // Drangedal
                    "5057", // Ørland
                    "3030", // Lillestrøm
                    "5441", // Deatnu/Tana
                    "3020", // Nordre Follo
                    "4626", // Øygarden
                    "4222", // Bykle
                    "4221", // Valle
                    "4224", // Åseral
                    "4223", // Vennesla
                    "4218", // Iveland
                    "3804", // Sandefjord
                    "1560", // Tingvoll
                    "1566", // Surnadal
                    "4211", // Gjerstad
                    "1563", // Sunndal
                    "4647", // Sunnfjord
                    "4220", // Bygland
                    "5054", // Indre Fosen
                    "1573", // Smøla
                    "1576", // Aure
                    "5032", // Selbu
                    "5033", // Tydal
                    "5034", // Meråker
                    "5035", // Stjørdal
                    "5036", // Frosta
                    "3027", // Rælingen
                    "1853", // Evenes
                    "1806", // Narvik
                    "5414", // Gratangen
                    "5421", // Senja
                    "3822", // Nissedal
                    "3821", // Kvitseid
                    "3824", // Tokke
                    "3825", // Vinje
                    "3823", // Fyresdal
                    "3820", // Seljord
                    "4213", // Tvedestrand
                    "1870", // Sortland
                    "3442", // Østre Toten
                    "4212", // Vegårshei
                    "3041", // Gol
                    "3042", // Hemsedal
                    "3039", // Flå
                    "3040", // Nesbyen
                    "3043", // Ål
                    "3044", // Hol
                    "5020", // Osen
                    "5058", // Åfjord
                    "5056", // Hitra
                    "5014", // Frøya
                    "5403", // Alta
                    "3014", // Indre Østfold
                    "4214", // Froland
                    "4217", // Åmli
                    "1506", // Molde
                    "1120", // Klepp
                    "1121", // Time
                    "4201", // Risør
                    "4202", // Grimstad
                    "5426"  // Gáivuotna/Kåfjord
            ));


    /**
     * Angir kommune som søker må være bosatt i for å kunne komme inn på løsningen.
     *
     * @return Liste med kommunenumre.
     */
    public static List<String> getDigisoskommuner() {
        return isNonProduction() ?  TEST_DIGISOS_KOMMUNER : PROD_DIGISOS_KOMMUNER;
    }

    private static boolean isNonProduction() {
        String miljo = System.getProperty("environment.name", "");
        return miljo.contains("q") || miljo.equals("test") || miljo.equals("dev-gcp") || miljo.equals("labs-gcp") || miljo.equals("local");
    }
}
