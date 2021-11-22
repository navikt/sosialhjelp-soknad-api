package no.nav.sosialhjelp.soknad.client.kodeverk

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService.Companion.SPRAAKKODE_NB
import no.nav.sosialhjelp.soknad.client.kodeverk.dto.BeskrivelseDto
import no.nav.sosialhjelp.soknad.client.kodeverk.dto.BetydningDto
import no.nav.sosialhjelp.soknad.client.kodeverk.dto.KodeverkDto
import no.nav.sosialhjelp.soknad.client.redis.KODEVERK_LAST_POLL_TIME_KEY
import no.nav.sosialhjelp.soknad.client.redis.KOMMUNER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

internal class KodeverkServiceTest {

    private val kommunenr1 = "1234"
    private val kommunenr2 = "5678"
    private val oslo = "Oslo"
    private val bergen = "Bergen"

    private val landkode1 = "NOR"
    private val landkode2 = "SWE"
    private val norge = "NORGE"
    private val sverige = "SVERIGE"

    private val postnummer1 = "0212"
    private val postnummer2 = "5050"

    private val kodeverkClient: KodeverkClient = mockk()
    private val redisService: RedisService = mockk()
    private val kodeverkService = KodeverkService(kodeverkClient, redisService)

    @Test
    internal fun `skal hente kommunenummer for kommunenavn fra client`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns null
        every { kodeverkClient.hentKommuner() } returns kommuneKodeverk

        val kommunenummer = kodeverkService.gjettKommunenummer(oslo)

        assertThat(kommunenummer).isNotNull
        assertThat(kommunenummer).isEqualTo(kommunenr1)
    }

    @Test
    internal fun `skal hente kommunenummer for kommunenavn fra cache`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns LocalDateTime.now().minusMinutes(1).format(ISO_LOCAL_DATE_TIME)
        every { redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto::class.java) } returns kommuneKodeverk

        val kommunenummer = kodeverkService.gjettKommunenummer(bergen)

        assertThat(kommunenummer).isNotNull
        assertThat(kommunenummer).isEqualTo(kommunenr2)
    }

    @Test
    internal fun `skal feile hvis consumer og cache gir null`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns LocalDateTime.now().minusMinutes(61).format(ISO_LOCAL_DATE_TIME)
        every { redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto::class.java) } returns null
        every { kodeverkClient.hentKommuner() } returns null

        val kommunenummer = kodeverkService.gjettKommunenummer(oslo)

        assertThat(kommunenummer).isNull()
    }

    @Test
    internal fun `skal feile hvis term ikke finnes`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns null
        every { kodeverkClient.hentKommuner() } returns kommuneKodeverk

        val kommunenummer = kodeverkService.gjettKommunenummer("ukjentKommunenavn")

        assertThat(kommunenummer).isNull()
    }

    @Test
    internal fun `skal hente poststed fra postnummer fra client`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns null
        every { kodeverkClient.hentPostnummer() } returns postnummerKodeverk

        val poststed = kodeverkService.getPoststed(postnummer1)

        assertThat(poststed).isNotNull
        assertThat(poststed).isEqualTo(oslo)
    }

    @Test
    internal fun `skal hente land fra landkode fra client`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns null
        every { kodeverkClient.hentLandkoder() } returns landkoderKodeverk

        val land = kodeverkService.getLand(landkode1)

        assertThat(land).isNotNull
        assertThat(land).isEqualTo("Norge")
    }

    @Test
    internal fun `skal feile hvis kodeverdi ikke finnes`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns null
        every { kodeverkClient.hentLandkoder() } returns landkoderKodeverk

        val land = kodeverkService.getLand("ukjentLandkode")

        assertThat(land).isNull()
    }

    @Test
    internal fun `skal formatere land korrekt`() {
        every { redisService.getString(KODEVERK_LAST_POLL_TIME_KEY) } returns null
        every { kodeverkClient.hentLandkoder() } returns landkoderKodeverk

        val land = kodeverkService.getLand("WLF")
        val land2 = kodeverkService.getLand("STP")
        val land3 = kodeverkService.getLand("PNG")

        assertThat(land).isEqualTo("Wallis/Futunaøyene")
        assertThat(land2).isEqualTo("Sao Tome og Principe")
        assertThat(land3).isEqualTo("Papua Ny-Guinea")
    }

    private val kommuneKodeverk: KodeverkDto
        get() {
            val now = LocalDate.now()
            return KodeverkDto(
                mapOf(
                    kommunenr1 to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto(oslo, oslo)))),
                    kommunenr2 to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto(bergen, bergen))))
                )
            )
        }

    private val landkoderKodeverk: KodeverkDto
        get() {
            val now = LocalDate.now()
            return KodeverkDto(
                mapOf(
                    landkode1 to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto(norge, norge)))),
                    landkode2 to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto(sverige, sverige)))),
                    "WLF" to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto("WALLIS/FUTUNAØYENE", "WALLIS/FUTUNAØYENE")))),
                    "STP" to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto("SAO TOME OG PRINCIPE", "SAO TOME OG PRINCIPE")))),
                    "PNG" to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto("PAPUA NY-GUINEA", "PAPUA NY-GUINEA"))))
                )
            )
        }

    private val postnummerKodeverk: KodeverkDto
        get() {
            val now = LocalDate.now()
            return KodeverkDto(
                mapOf(
                    postnummer1 to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto(oslo, oslo)))),
                    postnummer2 to listOf(BetydningDto(now, now, mapOf(SPRAAKKODE_NB to BeskrivelseDto(bergen, bergen))))
                )
            )
        }
}
