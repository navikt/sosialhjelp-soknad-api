package no.nav.sosialhjelp.soknad.common.mapper

import no.nav.sosialhjelp.soknad.common.MiljoUtils

object KommuneTilNavEnhetMapper {

    val IKS_KOMMUNER: Map<String, String> = mapOf(
        "3438" to "Nord-Fron", // Sør-Fron (nytt kommunenummer)
        "3439" to "Nord-Fron", // Ringebu (nytt kommunenummer)
        "3050" to "Rollag", // Flesberg (nytt kommunenummer)
        "3052" to "Rollag", // Nore og Uvdal (nytt kommunenummer)
        "1151" to "Haugesund", // Utsira (samme kommunenummer etter 2020)
        "4207" to "Kvinesdal", // Flekkefjord
        "4225" to "Kvinesdal", // Lyngdal
        "4206" to "Kvinesdal", // Farsund
        "4228" to "Kvinesdal", // Sirdal
        "4218" to "Vennesla", // Iveland
        "4224" to "Vennesla", // Åseral
        "4221" to "Vennesla", // Valle
        "4222" to "Vennesla", // Bykle
        "4220" to "Vennesla", // Bygland
        "5032" to "Stjørdal", // Selbu
        "5033" to "Stjørdal", // Tydal
        "5034" to "Stjørdal", // Meråker
        "5036" to "Stjørdal", // Frosta
        "1853" to "Narvik", // Evenes
        "5414" to "Narvik", // Gratangen
        "3822" to "Tokke", // Nissedal
        "3821" to "Tokke", // Kvitseid
        "3825" to "Tokke", // Vinje
        "3823" to "Tokke", // Fyresdal
        "3820" to "Tokke", // Seljord
        "3042" to "Gol", // Hemsedal
        "3039" to "Gol", // Flå
        "3040" to "Gol", // Nesbyen
        "3043" to "Gol", // Ål
        "3044" to "Gol", // Hol
        "5058" to "Osen", // Åfjord
        "1121" to "Klepp", // Time
    )

    private val PROD_ORGANISASJONSNUMMER: Map<String, String> = mapOf(
        "0701" to "974605171", // Horten
        "1247" to "974600889", // Askøy
        "0806" to "995509970", // Skien
        "0326" to "970534644", // Alna, Oslo kommune
        "0330" to "974778874", // Bjerke, Oslo kommune
        "0312" to "874778702", // Frogner, Oslo kommune
        "0316" to "974778742", // Gamle Oslo, Oslo kommune
        "0328" to "974778866", // Grorud, Oslo kommune
        "0315" to "870534612", // Grünerløkka, Oslo kommune
        "0331" to "974778882", // Nordre Aker, Oslo kommune
        "0318" to "970534679", // Nordstrand, Oslo kommune
        "0314" to "974778726", // Sagene, Oslo kommune
        "0313" to "971179686", // St. Hanshaugen, Oslo kommune
        "0327" to "874778842", // Stovner, Oslo kommune
        "0319" to "972408875", // Søndre Nordstrand, Oslo kommune
        "0335" to "971022051", // Ullern, Oslo kommune
        "0334" to "970145311", // Vestre Aker, Oslo kommune
        "0321" to "974778807", // Østensjø, Oslo kommune
        "5701" to "892284792", // Falkenborg, Trondheim kommune
        "5702" to "992284838", // Lerkendal, Trondheim kommune & Klæbu
        "1161" to "873864192", // Eiganes og Tasta, Stavanger kommune
        "1164" to "976670531", // Hillevåg og Hinna, Stavanger kommune
        "1162" to "973864181", // Hundvåg og Storhaug, Stavanger kommune
        "1165" to "973864203", // Madla, Stavanger kommune
        "1169" to "874617512", // Rennesøy og Finnøy, Stavanger kommune
        "1102" to "874610712", // Sandnes
        "1119" to "976827961", // Hå
        "1124" to "948243113", // Sola
        "5303" to "997784618", // Larvik
        "0403" to "974623811", // Hamar
        "1122" to "974616564", // Gjesdal
        "0529" to "986838961", // Vestre Toten
        "0626" to "974545861", // Lier
        "1866" to "874052582", // Hadsel
        "1133" to "974549751", // Hjelmeland
        "1135" to "974617250", // Sauda
        "1130" to "974616734", // Strand
        "0624" to "994952854", // Øvre Eiker
        "1134" to "974616998", // Suldal
        "0532" to "974596016", // Jevnaker
        "1401" to "974551918", // Kinn
        "1238" to "944233199", // Kvam
        "1933" to "921858361", // Balsfjord og Storfjord
        "0221" to "976637216", // Aurskog-Høland
        "1224" to "993975192", // Kvinnherad
        "1824" to "876834162", // Vefsn
        "0815" to "979525095", // Kragerø
        "0516" to "974543303", // Nord-Fron, Sør-Fron, Ringebu
        "2004" to "974601753", // Hammerfest
        "1127" to "988052310", // Randaberg-Kvitsøy
        "0412" to "976639618", // Ringsaker
        "0426" to "974550342", // Våler (Hedmark)
        "1037" to "964964076", // Kvinesdal, Flekkefjord, Lyngdal, Farsund, Sirdal
        "0106" to "993393851", // Fredrikstad
        "0605" to "976820835", // Ringerike
        "0418" to "964950768", // Nord-Odal
        "1235" to "973951270", // Voss
        "2003" to "974622238", // Vadsø
        "1243" to "992179457", // Bjørnafjorden
        "0229" to "974604175", // Enebakk
        "0517" to "974562294", // Sel
        "0513" to "976641175", // Lom-Skjåk
        "1219" to "834210622", // Bømlo
        "0515" to "976641310", // Vågå
        "0502" to "974567776", // Gjøvik
        "0906" to "976825950", // Arendal
        "1903" to "978608418", // Harstad
        "0511" to "919059303", // Lesja-Dovre
        "1804" to "983942962", // Bodø
        "1913" to "959469326", // Skånland, Tjelsund
        "0417" to "994743767", // Stange
        "1702" to "983921000", // Steinkjer, Snåase-Snåsa, Inderøy
        "0211" to "974574306", // Vestby
        "1222" to "974575728", // Fitjar
        "1517" to "978607063", // Hareid
        "1516" to "978607063", // Ulstein
        "1514" to "978607063", // Sande
        "0105" to "974560593", // Sarpsborg
        "1221" to "991459634", // Stord
        "0827" to "973802003", // Hjartdal
        "0807" to "984001797", // Notodden
        "0632" to "964963282", // Rollag, Flesberg, Nore og Uvdal
        "0604" to "974572486", // Kongsberg
        "0826" to "874548472", // Tinn
        "0427" to "976640322", // Elverum
        "0237" to "974604442", // Eidsvoll
        "0239" to "939780777", // Hurdal
        "0423" to "964948143", // Grue
        "1548" to "974545284", // Hustadvika
        "0128" to "997220838", // Rakkestad
        "0236" to "976665589", // Nes (Akershus)
        "0234" to "974547678", // Gjerdrum
        "0101" to "959159092", // Halden
        "0235" to "983870953", // Ullensaker
        "0238" to "874604682", // Nannestad
        "1833" to "993576190", // Rana
        "1149" to "995075199", // Karmøy,Bokn
        "1515" to "983931073", // Herøy, Vanylven
        "1106" to "974575906", // Haugesund, Utsira
        "1663" to "974624257", // Malvik
        "1146" to "974617757", // Tysvær
        "0536" to "974596393", // Søndre Land
        "0219" to "974702401", // Bærum
        "1416" to "976831322", // Høyanger
        "0402" to "944117784", // Kongsvinger
        "0419" to "874602612", // Sør-Odal
        "0215" to "974600544", // Frogn
        "1719" to "974556464", // Levanger
        "0230" to "976637992", // Lørenskog
        "0420" to "874602752", // Eidskog
        "1438" to "973981749", // Bremanger
        "1443" to "974556154", // Stad
        "1445" to "976675185", // Gloppen
        "1449" to "976831772", // Stryn
        "1505" to "974775719", // Kristiansund
        "1554" to "962378064", // Averøy
        "1557" to "974782170", // Gjemnes
        "1942" to "974574101", // Nordreisa
        "1941" to "976835441", // Skjervøy
        "0425" to "974602970", // Åsnes
        "0135" to "974569906", // Råde
        "1002" to "921060440", // Nye Lindesnes
        "5302" to "974607395", // Færder
        "0704" to "974586002", // Tønsberg
        "5301" to "974542811", // Holmestrand
        "1504" to "975914895", // Ålesund, Fjord
        "1532" to "974619148", // Giske
        "1923" to "974597578", // Salangen, Lavangen og Dyrøy
        "0542" to "994057790", // Nord-Aurdal, Sør-Aurdal, Etnedal, Vestre Slidre, Øystre Slidre, Vang
        "0538" to "974546302", // Nordre Land
        "0817" to "974551063", // Drangedal
        "1621" to "974620529", // Ørland
        "0231" to "974555379", // Lillestrøm
        "2025" to "987590254", // Deatnu/Tana
        "0213" to "974593408", // Nordre Follo
        "1246" to "974586312", // Øygarden
        "1014" to "936846777", // Bykle, Bygland, Valle, Åseral, Vennesla og Iveland
        "0710" to "974594188", // Sandefjord
        "1560" to "916359616", // Tingvoll
        "1566" to "974619962", // Surnadal
        "0911" to "964964998", // Gjerstad
        "1563" to "813112892", // Sunndal
        "1432" to "976831683", // Sunnfjord
        "5703" to "874577502", // Indre Fosen
        "1573" to "976832418", // Smøla
        "1576" to "921358288", // Aure
        "1783" to "918964118", // Selbu, Tydal, Meråker, Stjørdal og Frosta
        "0228" to "976637437", // Rælingen
        "1805" to "974592630", // Narvik, Evenes og Gratangen
        "1931" to "974562820", // Senja
        "0833" to "974610175", // Nissedal, Kvitseid, Tokke, Vinje, Fyresdal og Seljord
        "0528" to "974604264", // Østre Toten
        "1870" to "974545713", // Sortland
        "0914" to "964965781", // Tvedestrand
        "0912" to "964965048", // Vegårshei
        "0617" to "974607948", // Gol, Hemsedal, Flå, Nesbyen, Ål og Hol
        "1630" to "974578069", // Osen og Åfjord
        "1620" to "874560332", // Hitra og Frøya
        "2012" to "974580713", // Alta
        "0124" to "998432189", // Indre Østfold
        "0919" to "946439045", // Froland
        "0929" to "864965962", // Åmli
        "1502" to "986852522", // Molde
        "1120" to "974549611", // Klepp og Time
        "0901" to "964977402", // Risør
        "0904" to "994161105", // Grimstad
        "1940" to "976979958", // Gáivuotna/Kåfjord
    )

    private val TEST_ORGANISASJONSNUMMER: Map<String, String> = mapOf(
        "0701" to "910940066", // Horten
        "1247" to "910230182", // Askøy
        "0312" to "910229699", // Frogner,             Oslo kommune
        "0315" to "811213322", // Grünerløkka,         Oslo kommune - OBS: Brukes i mock!
        "0328" to "910229702", // Grorud,              Oslo kommune
        "0327" to "910589792", // Stovner,             Oslo kommune
        "0314" to "910565338", // Sagene,              Oslo kommune
        "0318" to "910309935", // Nordstrand,          Oslo kommune
        "0319" to "910723499", // Søndre Nordstrand,   Oslo kommune
        "0316" to "910229567", // Gamle Oslo,          Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0313" to "910229567", // St. Hanshaugen,      Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0335" to "910229567", // Ullern,              Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0334" to "910229567", // Vestre Aker,         Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0331" to "910229567", // Nordre Aker,         Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0330" to "910229567", // Bjerke,              Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0326" to "910229567", // Alna,                Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0321" to "910229567", // Østensjø,            Oslo kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0219" to "910231065", // Bærum
        "5701" to "910230646", // NAV Falkenborg, Trondheim kommune
        "5702" to "910230611", // NAV Lerkendal, Trondheim kommunne
        "1161" to "910229567", // Eiganes og Tasta, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "1164" to "910229567", // Hillevåg og Hinna, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "1162" to "910229567", // Hundvåg og Storhaug, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "1165" to "910229567", // Madla, Stavanger kommune - OBS: Sendes til vårt orgnummer i FIKS!
        "0403" to "910229567", // Hamar - OBS: Sendes til vårt orgnummer i FIKS!
        "0516" to "910229567", // Nord-Fron - OBS: Sendes til vårt orgnummer i FIKS!
        "0519" to "910229567", // Sør-Fron - OBS: Sendes til vårt orgnummer i FIKS!
        "0520" to "910229567", // Ringebu - OBS: Sendes til vårt orgnummer i FIKS!
        "1517" to "910229567", // NAV Hareid - Ulstein - Sande - OBS: Sendes til vårt orgnummer i FIKS!
    )

    fun getOrganisasjonsnummer(enhetNr: String?): String? {
        return if (MiljoUtils.isNonProduction()) TEST_ORGANISASJONSNUMMER[enhetNr] else PROD_ORGANISASJONSNUMMER[enhetNr]
    }

    private val TEST_DIGISOS_KOMMUNER = listOf(
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
        "1514" // Nytt Sande i Møre og Romsdal
    )
    private val PROD_DIGISOS_KOMMUNER = listOf(
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
        "5426" // Gáivuotna/Kåfjord
    )

    /**
     * Angir kommune som søker må være bosatt i for å kunne komme inn på løsningen.
     *
     * @return Liste med kommunenumre.
     */
    val digisoskommuner: List<String>
        get() = if (MiljoUtils.isNonProduction()) TEST_DIGISOS_KOMMUNER else PROD_DIGISOS_KOMMUNER
}
