package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.client.redis.KOMMUNEINFO_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.KOMMUNEINFO_LAST_POLL_TIME_KEY
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class KommuneInfoServiceTest {

    private val kommuneInfoClient: KommuneInfoClient = mockk()
    private val redisService: RedisService = mockk()

    private val kommuneInfoService = KommuneInfoService(kommuneInfoClient, redisService)

    private val KOMMUNENR = "1234"
    private val KOMMUNENR_UTEN_KONFIG = "1111"
    private val KOMMUNENR_MED_KONFIG = "2222"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { redisService.getString(any()) } returns null
        every { redisService.setex(KOMMUNEINFO_CACHE_KEY, any(), any()) } just Runs
        every { redisService.set(KOMMUNEINFO_LAST_POLL_TIME_KEY, any()) } just Runs
    }

    @Test
    internal fun kommuneUtenKonfigurasjonSkalGikanMottaSoknaderFalse() {
        val kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        val kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_UTEN_KONFIG)
        assertThat(kanMottaSoknader).isFalse
    }

    @Test
    internal fun kommuneMedKonfigurasjonSkalGikanMottaSoknaderLikKonfigurasjon() {
        // True
        var kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        var kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_MED_KONFIG)
        assertThat(kanMottaSoknader).isTrue

        // False
        kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, false, false, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_MED_KONFIG)
        assertThat(kanMottaSoknader).isFalse
    }

    @Test
    internal fun kommuneUtenKonfigurasjonSkalGiharMidlertidigDeaktivertMottakFalse() {
        val kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        val harMidlertidigDeaktivertMottak = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_UTEN_KONFIG)
        assertThat(harMidlertidigDeaktivertMottak).isFalse
    }

    @Test
    internal fun kommuneMedKonfigurasjonSkalGiharMidlertidigDeaktivertMottakLikKonfigurasjon() {
        // True
        var kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        var kanMottaSoknader = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_MED_KONFIG)
        assertThat(kanMottaSoknader).isTrue

        // False
        kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        kanMottaSoknader = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_MED_KONFIG)
        assertThat(kanMottaSoknader).isFalse
    }

    @Test
    internal fun kommuneInfo_fiks_feiler_og_cache_er_tom() {
        every { kommuneInfoClient.getAll() } returns emptyList()
        every { redisService.getKommuneInfos() } returns null

        val kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE)
    }

    @Test
    internal fun kommuneInfo_case1_ingen_konfigurasjon() {
        // Case 1
        val kommuneInfo = KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        val kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR_UTEN_KONFIG)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.MANGLER_KONFIGURASJON)
    }

    @Test
    internal fun kommuneInfo_case2_deaktivert_mottak_8_permutasjoner_0000_0111() {
        // Kun deaktivert mottak (permutasjon 0 = 0000)
        var value = KommuneInfo(KOMMUNENR, false, false, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        var kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. midlertidig deaktivert innsyn (permutasjon 1 = 0001)
        value = KommuneInfo(KOMMUNENR, false, false, false, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. midlertidig deaktivert mottak (permutasjon 2 = 0010)
        value = KommuneInfo(KOMMUNENR, false, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 3 = 0011)
        value = KommuneInfo(KOMMUNENR, false, false, true, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn (permutasjon 4 = 0100)
        value = KommuneInfo(KOMMUNENR, false, true, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn og midlertidig deaktivert innsyn (permutasjon 5 = 0101)
        value = KommuneInfo(KOMMUNENR, false, true, false, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak (permutasjon 6 = 0110)
        value = KommuneInfo(KOMMUNENR, false, true, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 7 = 0111)
        value = KommuneInfo(KOMMUNENR, false, true, true, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)
    }

    @Test
    internal fun kommuneInfo_case3_aktivert_mottak() {
        // Kun aktivert mottak (permutasjon 8 = 1000)
        var value = KommuneInfo(KOMMUNENR, true, false, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        var kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)

        // Inkl. deaktivert innsyn (permutasjon 9 = 1001)
        value = KommuneInfo(KOMMUNENR, true, false, false, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)
    }

    @Test
    internal fun kommuneInfo_case4_aktivert_mottak_og_innsyn() {
        // Case 4 (permutasjon 12 = 1100)
        var value = KommuneInfo(KOMMUNENR, true, true, false, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        var kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)

        // Inkl. midlertidig deaktivert innsyn (permutasjon 13 = 1101)
        value = KommuneInfo(KOMMUNENR, true, true, false, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)
    }

    @Test
    internal fun kommuneInfo_case5_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak() {
        // Case 5 (permutasjon 14 = 1110)
        var value = KommuneInfo(KOMMUNENR, true, true, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        var kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)

        // Inkl. deaktivert mottak (permutasjon 10 = 1010)
        value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)

        // Inkl. deaktivert innsyn (permutasjon 11 = 1011)
        value = KommuneInfo(KOMMUNENR, true, false, true, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)
    }

    @Test
    internal fun kommuneInfo_case6_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak_og_innsyn() {
        // Case 6 (permutasjon 15 = 1111)
        val value = KommuneInfo(KOMMUNENR, true, true, true, true, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        val kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)
    }

    @Test
    internal fun behandlingsansvarligKommuneSkalReturneresUtenKommuneINavnet() {
        val value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, "nabokommunenavn kommune")
        every { kommuneInfoClient.getAll() } returns listOf(value)

        val kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn")
        assertThat(kommunenavn).isEqualTo("nabokommunenavn")
    }

    @Test
    internal fun behandlingsansvarligKommuneSkalReturnereKommunenavnHvisIngenBehandlingsansvarlig() {
        val value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        val kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn")
        assertThat(kommunenavn).isEqualTo("kommunenavn")
    }

    @Test
    internal fun behandlingsansvarligKommuneSkalReturnereKommunenavnHvisIngenBehandlingsansvarligOgKommuneInfoMapErNull() {
        every { kommuneInfoClient.getAll() } returns emptyList()
        every { redisService.getKommuneInfos() } returns null

        val kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn")
        assertThat(kommunenavn).isEqualTo("kommunenavn")
    }

    @Test
    internal fun skalHenteKommuneInfoFraCache_hvisLastTimePollErInnenfor() {
        val value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null)
        val kommuneInfoMap = mapOf(KOMMUNENR to value)

        every { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) } returns LocalDateTime.now().minusMinutes(2)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { redisService.getKommuneInfos() } returns kommuneInfoMap

        kommuneInfoService.kanMottaSoknader(KOMMUNENR)

        verify(exactly = 1) { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) }
        verify(exactly = 1) { redisService.getKommuneInfos() }
    }

    @Test
    internal fun skalHenteKommuneInfoFraFiks_hvisLastPollTimeOverskriderGrense() {
        val value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null)
        every { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) } returns LocalDateTime.now().minusMinutes(12)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { kommuneInfoClient.getAll() } returns listOf(value)

        kommuneInfoService.kanMottaSoknader(KOMMUNENR)

        verify(exactly = 1) { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) }
        verify(exactly = 0) { redisService.getKommuneInfos() }
    }

    @Test
    internal fun hentKommuneInfoFraFiksFeiler_brukCache() {
        val value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null)
        val kommuneInfoMap = mapOf(KOMMUNENR to value)

        every { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) } returns LocalDateTime.now().minusMinutes(12)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { kommuneInfoClient.getAll() } returns emptyList()
        every { redisService.getKommuneInfos() } returns kommuneInfoMap

        val kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR)

        assertThat(kanMottaSoknader).isTrue

        verify(exactly = 1) { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) }
        verify(exactly = 1) { redisService.getKommuneInfos() }
    }

    @Test
    internal fun hentKommuneInfoFraFiksFeiler_cacheErTom() {
        every { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) } returns LocalDateTime.now().minusMinutes(12)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        every { kommuneInfoClient.getAll() } returns emptyList()
        every { redisService.getKommuneInfos() } returns null

        val kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR)

        assertThat(kanMottaSoknader).isFalse

        verify(exactly = 1) { redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY) }
        verify(exactly = 1) { redisService.getKommuneInfos() }
    }

    @Test
    internal fun hentAlleKommuneInfo_fiksFeiler_skalHenteFraCache() {
        val value = KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null)
        val cachedKommuneInfoMap = mapOf(KOMMUNENR to value)

        every { kommuneInfoClient.getAll() } returns emptyList()
        every { redisService.getKommuneInfos() } returns cachedKommuneInfoMap

        val kommuneInfoMap = kommuneInfoService.hentAlleKommuneInfo()
        assertThat(kommuneInfoMap).isEqualTo(cachedKommuneInfoMap)
    }
}
