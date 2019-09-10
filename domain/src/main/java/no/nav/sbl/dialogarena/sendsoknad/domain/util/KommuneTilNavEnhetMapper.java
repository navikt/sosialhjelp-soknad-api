package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;


public class KommuneTilNavEnhetMapper {

    private static final Logger log = LoggerFactory.getLogger(KommuneTilNavEnhetMapper.class);

    public static final Map<String, String> IKS_KOMMUNER = new ImmutableMap.Builder<String, String>()
            .put("0519", "Nord-Fron") // Sør-Fron
            .put("0520", "Nord-Fron") // Ringebu
            .put("0631", "Rollag")    // Flesberg
            .put("0633", "Rollag")    // Nore og Uvdal
            .build();

    private static final Map<String, String> PROD_ORGANISASJONSNUMMER = new ImmutableMap.Builder<String, String>()
            .put("0701", "974605171")    // Horten
            .put("1247", "974600889")    // Askøy
            .put("0806", "995509970")    // Skien
            .put("1204", "976829786")    // Arna, Bergen kommune
            .put("1209", "976830563")    // Bergenhus, Bergen kommune
            .put("1202", "976829948")    // Fana, Bergen kommune
            .put("1205", "976830032")    // Fyllingsdalen, Bergen kommune
            .put("1206", "976830121")    // Laksevåg, Bergen kommune
            .put("1210", "976830652")    // Ytrebygda, Bergen kommune
            .put("1208", "976830172")    // Årstad, Bergen kommune
            .put("1203", "976830784")    // Åsane, Bergen kommune
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
            .put("1401", "974551918")    // Flora
            .put("1238", "944233199")    // Kvam
            .put("1933", "921858361")    // Balsfjord og Storfjord
            .put("0221", "976637216")    // Aurskog-Høland
            .put("1224", "993975192")    // Kvinnherad
            .put("1824", "876834162")    // Vefsn
            .put("0815", "979525095")    // Kragerø
            .put("0516", "974543303")    // Nord-Fron
            .put("0519", "974543303")    // Sør-Fron
            .put("0520", "974543303")    // Ringebu
            .put("2004", "974601753")    // Hammerfest
            .put("1127", "988052310")    // Randaberg-Kvitsøy
            .put("0412", "976639618")    // Ringsaker
            .put("0426", "974550342")    // Våler (Hedmark)
            .put("1037", "964964076")    // Kvinesdal
            .put("0106", "993393851")    // Fredrikstad
            .put("0605", "976820835")    // Ringerike
            .put("0418", "964950768")    // Nord-Odal
            .put("1235", "973951270")    // Voss
            .put("2003", "974622238")    // Vadsø
            .put("1243", "992179457")    // Os (Hordaland)
            .put("0229", "974604175")    // Enebakk
            .put("1002", "964968519")    // Mandal
            .put("0514", "974592274")    // Lom
            .put("0517", "974562294")    // Sel
            .put("0513", "976641175")    // Skjåk
            .put("1219", "834210622")    // Bømlo
            .put("0515", "976641310")    // Vågå
            .put("0502", "974567776")    // Gjøvik
            .put("0906", "976825950")    // Arendal
            .put("1903", "978608418")    // Harstad
            .put("0511", "919059303")    // Dovre
            .put("0512", "964949204")    // Lesja
            .put("1804", "983942962")    // Bodø
            .put("1913", "959469326")    // Skånland
            .put("1852", "959469326")    // Tjelsund
            .put("0417", "994743767")    // Stange
            .put("1702", "983921000")    // Steinkjer, Verran, Snåsa, Inderøy
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
            .put("1029", "964966664")    // Lindesnes
            .put("0423", "964948143")    // Grue
            .put("1021", "964966931")    // Marnardal
            .put("1548", "974545284")    // Fræna, Eide
            .put("0128", "997220838")    // Rakkestad
            .put("0236", "938679088")    // Nes (Akershus)
            .put("0234", "991378111")    // Gjerdrum
            .put("0101", "959159092")    // Halden
            .put("0235", "933649768")    // Ullensaker
            
        
            .build();

    private static final Map<String, String> TEST_ORGANISASJONSNUMMER = new ImmutableMap.Builder<String, String>()
            .put("0701", "910940066")   // Horten
            .put("1208", "910230964")   // Årstad, Bergen kommune
            .put("1209", "910230158")   // Bergenhus, Bergen kommune
            .put("1210", "910230506")   // Ytrebygda, Bergen kommune
            .put("1202", "910230913")   // Fana, Bergen kommune
            .put("0312", "910229699")   // Frogner, Oslo kommune
            .put("1247", "910230182")   // Askøy
            .put("0315", "811213322")   // Grünerløkka, Oslo kommune
            .put("0328", "910229702")   // Grorud, Oslo kommune
            .put("0327", "910589792")   // Stovner, Oslo kommune
            .put("0314", "910565338")   // Sagene, Oslo kommune

            .put("0318", "910309935")   // Nordstrand, Oslo kommune
            .put("0319", "910723499")   // Søndre Nordstrand, Oslo kommune
            .put("0219", "910231065")   // Bærum
            .put("1204", "910230905")   // Arna, Bergen kommune
            .put("1203", "910230530")   // Åsane, Bergen kommune
            .put("1205", "910230948")   // Fyllingsdalen, Bergen kommune
            .put("1206", "910230514")   // Laksevåg, Bergen kommune
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

            .build();

    public static String getOrganisasjonsnummer(String enhetNr) {
        return isProduction() ? PROD_ORGANISASJONSNUMMER.get(enhetNr) : TEST_ORGANISASJONSNUMMER.get(enhetNr);
    }

    private static final List<String> TEST_DIGISOS_KOMMUNER = Collections.unmodifiableList(
            asList(
                    "0701",
                    "0703",
                    "0717",
                    "1201",
                    "0301",
                    "1247",
                    "0219",
                    "5001",
                    "1103",
                    "0403",
                    "0516",
                    "0519",
                    "0520"
            ));

    private static final List<String> PROD_DIGISOS_KOMMUNER = Collections.unmodifiableList(
            asList(
                    "0701",
                    "0703",
                    "0717",
                    "0806",
                    "1201",
                    "0301",
                    "1247",
                    "5001",
                    "1103",
                    "1102",
                    "1119",
                    "1124",
                    "0712",
                    "0403",
                    "1122",
                    "0529",
                    "0626",
                    "1866",
                    "1133",
                    "1135",
                    "1130",
                    "0624",
                    "1134",
                    "0532",
                    "1401",
                    "1238",
                    "1939",
                    "5030",
                    "1933",
                    "1939",
                    "0221",
                    "1224",
                    "1824",
                    "0815",
                    "2004",
                    "1127",
                    "1144",
                    "0516",
                    "0519",
                    "0520",
                    "0412",
                    "0426",
                    "1037",
                    "0106",
                    "0605",
                    "0418",
                    "1235",
                    "1243",
                    "2003",
                    "0229",
                    "1002",
                    "0514",
                    "0517",
                    "0513",
                    "0515",
                    "1219",
                    "0502",
                    "0906",
                    "1903",
                    "0511",
                    "0512",
                    "1804",
                    "1913",
                    "1852",
                    "0417",
                    "5004",
                    "5039",
                    "5041",
                    "5053",
                    "0211",
                    "1222",
                    "1517",
                    "1516",
                    "1514",
                    "0105",
                    "1221",
                    "0827",
                    "0807",
                    "0633",
                    "0604",
                    "0826",
                    "0631",
                    "0632",
                    "0427",
                    "0237",
                    "0239",
                    "1029",
                    "0423",
                    "1021",
                    "1548",
                    "1551",
                    "0128",
                    "0236",
                    "0234",
                    "0101",
                    "0235"
                    
                
                
             
                
            ));


    /**
     * Angir kommune som søker må være bosatt i for å kunne komme inn på løsningen.
     *
     * @return Liste med kommunenumre.
     */
    public static List<String> getDigisoskommuner() {
        return isProduction() ? PROD_DIGISOS_KOMMUNER : TEST_DIGISOS_KOMMUNER;
    }

    private static boolean isProduction() {
        return "p".equals(System.getProperty("environment.name"));
    }
}
