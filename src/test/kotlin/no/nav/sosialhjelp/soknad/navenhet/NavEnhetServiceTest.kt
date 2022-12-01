package no.nav.sosialhjelp.soknad.navenhet

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.ws.rs.ServiceUnavailableException

internal class NavEnhetServiceTest {

    companion object {
        private const val GT = "0101"
        private const val ENHETSNUMMER = "0701"
        private const val ORGNUMMER_PROD = "974605171"
        private const val ORGNUMMER_TEST = "910940066"
    }

    private val norgClient: NorgClient = mockk()
    private val redisService: RedisService = mockk()
    private val navEnhetService = NavEnhetService(norgClient, redisService)

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
