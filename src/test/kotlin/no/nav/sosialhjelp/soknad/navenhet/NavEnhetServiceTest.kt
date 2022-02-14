package no.nav.sosialhjelp.soknad.navenhet

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.ws.rs.ServiceUnavailableException

internal class NavEnhetServiceTest {

    private val GT = "0101"
    private val ENHETSNUMMER = "0701"
    private val ORGNUMMER_PROD = "974605171"
    private val ORGNUMMER_TEST = "910940066"

    private val norgClient: NorgClient = mockk()
    private val redisService: RedisService = mockk()
    private val navEnhetService = NavEnhetServiceImpl(norgClient, redisService)

    private val navEnhetDto = NavEnhetDto("Nav Enhet", ENHETSNUMMER)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    internal fun finnEnhetForGtBrukerTestOrgNrForTest() {
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GT) } returns navEnhetDto
        every { redisService.getString(any()) } returns null
        val navEnhet = navEnhetService.getEnhetForGt(GT)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(ORGNUMMER_TEST)
    }

    @Test
    fun finnEnhetForGtBrukerOrgNrFraNorgForProd() {
        every { MiljoUtils.isNonProduction() } returns false
        every { redisService.getString(any()) } returns null
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GT) } returns navEnhetDto
        val navEnhet = navEnhetService.getEnhetForGt(GT)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(ORGNUMMER_PROD)
    }

    @Test
    fun finnEnhetForLom() {
        every { MiljoUtils.isNonProduction() } returns false
        val gt = "3434"
        val sosialOrgNummer = "974592274"
        val navEnhetDtoLom = NavEnhetDto("Nav Enhet", "0513")
        every { redisService.getString(any()) } returns null
        every { norgClient.hentNavEnhetForGeografiskTilknytning(gt) } returns navEnhetDtoLom
        val navEnhet = navEnhetService.getEnhetForGt(gt)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(sosialOrgNummer)
    }

    @Test
    fun finnEnhetForSkjaak() {
        every { MiljoUtils.isNonProduction() } returns false
        val gt = "3432"
        val sosialOrgNummer = "976641175"
        val navEnhetDtoSjaak = NavEnhetDto("Nav Enhet", "0513")
        every { redisService.getString(any()) } returns null
        every { norgClient.hentNavEnhetForGeografiskTilknytning(gt) } returns navEnhetDtoSjaak
        val navEnhet = navEnhetService.getEnhetForGt(gt)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(sosialOrgNummer)
    }

    @Test
    fun getEnheterForKommunenummer_forKommunerMedFlereNavenheter_skalReturnereRettAntall() {
        // Oslo
        var enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer("0301")
        assertThat(enheterForKommunenummer).hasSize(15)

        // Bergen
        enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer("4601")
        assertThat(enheterForKommunenummer).hasSize(8) // Ikke 5?

        // Stavanger
        enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer("1103")
        assertThat(enheterForKommunenummer).hasSize(5)

        // Trondheim
        enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer("5001")
        assertThat(enheterForKommunenummer).hasSize(2)
    }

    @Test
    fun getEnheterForKommunenummer_forKommunerEnNavenhet_skalReturnereEnNavenhet() {
        // Viken
        var kommuner = listOf("3001", "3002", "3003", "3004", "3005", "3006", "3007", "3011", "3012", "3013", "3014", "3015", "3016", "3017", "3018", "3019", "3020", "3021", "3022", "3023", "3024", "3025", "3026", "3027", "3028", "3029", "3030", "3031", "3032", "3033", "3034", "3035", "3036", "3037", "3038", "3039", "3040", "3041", "3042", "3043", "3044", "3045", "3046", "3047", "3048", "3049", "3050", "3051", "3052", "3053", "3054")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Innlandet
        kommuner = listOf("3401", "3403", "3405", "3407", "3411", "3412", "3413", "3414", "3415", "3416", "3417", "3418", "3419", "3420", "3421", "3422", "3423", "3424", "3425", "3426", "3427", "3428", "3429", "3430", "3431", "3432", "3433", "3434", "3435", "3436", "3437", "3438", "3439", "3440", "3441", "3442", "3443", "3446", "3447", "3448", "3449", "3450", "3451", "3452", "3453", "3454")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Vestfold og Telemark
        kommuner = listOf("3801", "3802", "3803", "3804", "3805", "3806", "3807", "3808", "3811", "3812", "3813", "3814", "3815", "3816", "3817", "3818", "3819", "3820", "3821", "3822", "3823", "3824", "3825")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Agder
        kommuner = listOf("4201", "4202", "4203", "4204", "4205", "4206", "4207", "4211", "4212", "4213", "4214", "4215", "4216", "4217", "4218", "4219", "4220", "4221", "4222", "4223", "4224", "4225", "4226", "4227", "4228")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Rogaland
        kommuner = listOf("1101", "1106", "1108", "1111", "1112", "1114", "1119", "1120", "1121", "1122", "1124", "1127", "1130", "1133", "1134", "1135", "1144", "1145", "1146", "1149", "1151", "1160")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Vestland
        kommuner = listOf("4602", "4611", "4612", "4613", "4614", "4615", "4616", "4617", "4618", "4619", "4620", "4621", "4622", "4623", "4624", "4625", "4626", "4627", "4628", "4629", "4630", "4631", "4632", "4633", "4634", "4635", "4636", "4637", "4638", "4639", "4640", "4641", "4642", "4643", "4644", "4645", "4646", "4647", "4648", "4649", "4650", "4651")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Møre og Romsdal
        kommuner = listOf("1505", "1506", "1507", "1511", "1514", "1515", "1516", "1517", "1520", "1525", "1528", "1531", "1532", "1535", "1539", "1539", "1547", "1554", "1557", "1560", "1563", "1566", "1573", "1573", "1576", "1577", "1578", "1579")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Trøndelag
        kommuner = listOf("5006", "5007", "5014", "5020", "5021", "5022", "5025", "5026", "5027", "5028", "5029", "5031", "5032", "5033", "5034", "5035", "5036", "5037", "5038", "5041", "5042", "5043", "5044", "5045", "5046", "5047", "5049", "5052", "5053", "5054", "5055", "5056", "5057", "5058", "5059", "5060", "5061")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Nordland
        kommuner = listOf("1804", "1806", "1812", "1813", "1815", "1816", "1818", "1820", "1822", "1824", "1825", "1826", "1827", "1828", "1832", "1833", "1834", "1835", "1836", "1837", "1838", "1839", "1840", "1841", "1845", "1848", "1851", "1853", "1856", "1857", "1859", "1860", "1865", "1866", "1867", "1868", "1870", "1871", "1874")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }

        // Troms og Finnmark
        kommuner = listOf("5401", "5402", "5403", "5404", "5405", "5406", "5411", "5412", "5413", "5414", "5415", "5416", "5417", "5418", "5419", "5420", "5421", "5422", "5423", "5424", "5425", "5426", "5427", "5428", "5429", "5430", "5432", "5433", "5434", "5435", "5436", "5437", "5438", "5439", "5440", "5441", "5442", "5443", "5444")
        kommuner.forEach {
            val enheterForKommunenummer = navEnhetService.getEnheterForKommunenummer(it)
            assertThat(enheterForKommunenummer).hasSize(1)
        }
    }

//    @Disabled
//    @Test
//    fun getAllNavenheterFromPath() {
//        val allNavenheterFromPath = navEnhetService.getAllNavEnheterFromPath()
//        var navenhetnavnUlikKommunenavn: MutableList<NavenhetFraLokalListe> = ArrayList()
//        val kommunenavnMedSpesifisertSted: MutableList<NavenhetFraLokalListe> = ArrayList()
//        val samiskeKommunenavn: MutableList<NavenhetFraLokalListe> = ArrayList()
//        val kommuneMap: MutableMap<String, MutableList<NavenhetFraLokalListe>> = HashMap()
//        val enhetsnavnMap: MutableMap<String, MutableList<NavenhetFraLokalListe>> = HashMap()
//        for (navenhet in allNavenheterFromPath.navEnheter) {
//            assertThat(navenhet.kommunenavn).isNotNull
//            assertThat(navenhet.kommunenavn).isNotBlank
//            if (!navenhet.enhetsnavn.contains(navenhet.kommunenavn) &&
//                navenhet.kommunenavn != "Oslo" &&
//                navenhet.kommunenavn != "Bergen" &&
//                navenhet.kommunenavn != "Stavanger" &&
//                navenhet.kommunenavn != "Trondheim"
//            ) {
//                if (navenhet.kommunenavn.contains(" - ") || navenhet.kommunenavn.contains(" – ")) {
//                    samiskeKommunenavn.add(navenhet)
//                } else if (navenhet.kommunenavn.contains(" i ")) {
//                    kommunenavnMedSpesifisertSted.add(navenhet)
//                } else {
//                    navenhetnavnUlikKommunenavn.add(navenhet)
//                }
//                var navenheterForEnhetsnavn = enhetsnavnMap[navenhet.enhetsnavn]
//                if (navenheterForEnhetsnavn == null) navenheterForEnhetsnavn = ArrayList()
//                navenheterForEnhetsnavn.add(navenhet)
//                enhetsnavnMap[navenhet.enhetsnavn] = navenheterForEnhetsnavn
//            }
//            var navenheterForKommune = kommuneMap[navenhet.kommunenavn]
//            if (navenheterForKommune == null) navenheterForKommune = ArrayList()
//            navenheterForKommune.add(navenhet)
//            kommuneMap[navenhet.kommunenavn] = navenheterForKommune
//        }
//        val kommunerMedFlereNavenheter = kommuneMap.entries
//            .stream()
//            .filter { (_, value): Map.Entry<String, List<NavenhetFraLokalListe>> -> value.size > 1 }
//            .collect(
//                Collectors.toMap<Map.Entry<String, List<NavenhetFraLokalListe>>, String, List<NavenhetFraLokalListe>>(
//                    Function<Map.Entry<String, List<NavenhetFraLokalListe>>, String> { (key, value) -> java.util.Map.Entry.key },
//                    Function<Map.Entry<String, List<NavenhetFraLokalListe>>, List<NavenhetFraLokalListe>> { (key, value) -> java.util.Map.Entry.value })
//            )
//        val enheterForFlerekommuner = enhetsnavnMap.entries
//            .stream()
//            .filter { (_, value): Map.Entry<String, List<NavenhetFraLokalListe>> -> value.size > 1 }
//            .collect(
//                Collectors.toMap<Map.Entry<String, List<NavenhetFraLokalListe>>, String, List<NavenhetFraLokalListe>?>(
//                    Function<Map.Entry<String, List<NavenhetFraLokalListe>>, String> { (key, value) -> java.util.Map.Entry.key },
//                    Function<Map.Entry<String, List<NavenhetFraLokalListe>>, List<NavenhetFraLokalListe>?> { (key, value) -> java.util.Map.Entry.value })
//            )
//        val navenheterOverFlereKommuner =
//            StringBuilder().append("\n~~~~~ Navenheter som strekker seg over flere kommuner: ~~~~~\n")
//        for ((navenhetNavn, navenheter): Map.Entry<String, List<NavenhetFraLokalListe>?> in enheterForFlerekommuner) {
//            navenheterOverFlereKommuner.append("\n").append(navenhetNavn).append(":\n")
//            navenheter.forEach(Consumer { navenhet: NavenhetFraLokalListe ->
//                navenheterOverFlereKommuner.append(
//                    "* "
//                ).append(navenhet.kommunenavn).append("\n")
//            })
//        }
//        println(navenheterOverFlereKommuner)
//        val samiskeKommuner = StringBuilder().append("\n~~~~~ Samiske kommuner: ~~~~~\n")
//        samiskeKommunenavn.forEach(Consumer { samiskKommune: NavenhetFraLokalListe ->
//            samiskeKommuner.append(samiskKommune.enhetsnavn).append(", ")
//            samiskeKommuner.append(samiskKommune.kommunenavn).append(" kommune").append("\n")
//        })
//        println(samiskeKommuner)
//        val spesifisertStedskommuner =
//            StringBuilder().append("\n~~~~~ Enhetsnavn ulik kommunenavn, der kommunen spesifiserer sted med i : ~~~~~\n")
//        kommunenavnMedSpesifisertSted.forEach(Consumer { kommune: NavenhetFraLokalListe ->
//            spesifisertStedskommuner.append(kommune.enhetsnavn).append(", ")
//            spesifisertStedskommuner.append(kommune.kommunenavn).append(" kommune").append("\n")
//        })
//        println(spesifisertStedskommuner)
//        navenhetnavnUlikKommunenavn = navenhetnavnUlikKommunenavn.stream().filter { navenhet: NavenhetFraLokalListe ->
//            enheterForFlerekommuner[navenhet.enhetsnavn] == null
//        }.collect(Collectors.toList()) // filtrere vekk navenheter som strekker seg over flere kommuner
//        val kommunerNavnUlikNavenhetsnavn = StringBuilder().append("\n~~~~~ Enhetsnavn ulik kommunenavn: ~~~~~\n")
//        navenhetnavnUlikKommunenavn.forEach(Consumer { kommune: NavenhetFraLokalListe ->
//            kommunerNavnUlikNavenhetsnavn.append(kommune.enhetsnavn).append(", ")
//            kommunerNavnUlikNavenhetsnavn.append(kommune.kommunenavn).append(" kommune").append("\n")
//        })
//        println(kommunerNavnUlikNavenhetsnavn)
//        assertThat(allNavenheterFromPath.navenheter.size)
//            .isEqualTo(356 + 17 - 1 + 8 - 1 + 9 - 1 + 4 - 1) //Totalt antall kommuner + ekstra navkontorer i Oslo, Bergen, Stavanger og Trondheim
//    }

    @Test
    fun skalHenteNavEnhetForGtFraConsumer() {
        every { MiljoUtils.isNonProduction() } returns false
        every { redisService.getString(any()) } returns null
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GT) } returns navEnhetDto
        val navEnhet = navEnhetService.getEnhetForGt(GT)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(ORGNUMMER_PROD)
        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GT) }
        verify(exactly = 1) { redisService.getString(any()) }
        verify(exactly = 0) { redisService.get(any(), any()) }
    }

    @Test
    fun skalHenteNavEnhetForGtFraCache() {
        every { MiljoUtils.isNonProduction() } returns false
        every { redisService.getString(any()) } returns LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { redisService.get(any(), any()) } returns navEnhetDto
        val navEnhet = navEnhetService.getEnhetForGt(GT)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(ORGNUMMER_PROD)
        verify(exactly = 0) { norgClient.hentNavEnhetForGeografiskTilknytning(GT) }
        verify(exactly = 1) { redisService.getString(any()) }
        verify(exactly = 1) { redisService.get(any(), any()) }
    }

    @Test
    fun skalBrukeCacheSomFallbackDersomConsumerFeilerOgCacheFinnes() {
        every { MiljoUtils.isNonProduction() } returns false
        every { redisService.getString(any()) } returns LocalDateTime.now().minusMinutes(60).minusSeconds(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { redisService.get(any(), any()) } returns navEnhetDto
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GT) } throws TjenesteUtilgjengeligException("norg feiler", ServiceUnavailableException())
        val navEnhet = navEnhetService.getEnhetForGt(GT)
        assertThat(navEnhet!!.sosialOrgNr).isEqualTo(ORGNUMMER_PROD)
        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GT) }
        verify(exactly = 1) { redisService.getString(any()) }
        verify(exactly = 1) { redisService.get(any(), any()) }
    }

    @Test
    fun skalKasteFeilHvisConsumerFeilerOgCacheErExpired() {
        every { redisService.getString(any()) } returns LocalDateTime.now().minusMinutes(60).minusSeconds(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { redisService.get(any(), any()) } returns null
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GT) } throws TjenesteUtilgjengeligException("norg feiler", ServiceUnavailableException())

        assertThatExceptionOfType(TjenesteUtilgjengeligException::class.java)
            .isThrownBy { navEnhetService.getEnhetForGt(GT) }

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GT) }
        verify(exactly = 1) { redisService.getString(any()) }
        verify(exactly = 1) { redisService.get(any(), any()) }
    }

    @Test
    fun skalReturnereNullHvisConsumerReturnererNull() {
        every { redisService.getString(any()) } returns LocalDateTime.now().minusMinutes(60).minusSeconds(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { norgClient.hentNavEnhetForGeografiskTilknytning(GT) } returns null

        val navEnhet = navEnhetService.getEnhetForGt(GT)
        assertThat(navEnhet).isNull()

        verify(exactly = 1) { norgClient.hentNavEnhetForGeografiskTilknytning(GT) }
        verify(exactly = 1) { redisService.getString(any()) }
        verify(exactly = 0) { redisService.get(any(), any()) } // sjekker ikke cache hvis consumer returnerer null (404 not found)
    }
}
