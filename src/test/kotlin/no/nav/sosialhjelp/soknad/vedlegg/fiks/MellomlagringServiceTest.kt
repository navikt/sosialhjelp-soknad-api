package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class MellomlagringServiceTest {

    private val mellomlagringClient: MellomlagringClient = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val virusScanner: VirusScanner = mockk()
    private val unleash: Unleash = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val mellomlagringStartString = "31.10.2022 12:00:00"
    private val mellomlagringStart = LocalDateTime.parse(mellomlagringStartString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))

    private val mellomlagringService = MellomlagringService(
        mellomlagringClient,
        soknadUnderArbeidRepository,
        virusScanner,
        unleash,
        kommuneInfoService,
        mellomlagringStartString
    )

    @BeforeEach
    internal fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.environmentName } returns "test"
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    internal fun `erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi - alle scenarier`() {
        // false - toggle er disabled
        every { unleash.isEnabled(any(), false) } returns false
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(mockk())).isFalse

        // false - soknadUnderArbeid er ettersendelse
        every { unleash.isEnabled(any(), false) } returns true
        val soknadUnderArbeid: SoknadUnderArbeid = mockk()
        every { soknadUnderArbeid.erEttersendelse } returns true
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)).isFalse

        // kast feil - mottakers kommunenummer er null
        every { soknadUnderArbeid.erEttersendelse } returns false
        every { soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer } returns null
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid) }

        // kast feil - nedetid for kommune
        every { soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer } returns "1234"
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid) }

        // kast feil - midlertidig nedetid for kommune
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException::class.java)
            .isThrownBy { mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid) }

        // false - kommune mangler konfigurasjon hos Fiks
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.MANGLER_KONFIGURASJON
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)).isFalse

        // false - kommune bruker SvarUt
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)).isFalse

        // false - soknad opprettet før mellomlagring ble aktivert
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        every { soknadUnderArbeid.opprettetDato } returns mellomlagringStart.minusSeconds(1)
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)).isFalse

        // true - soknad skal sendes med digisosApi - soknad opprettet samtidig med aktivering
        every { soknadUnderArbeid.opprettetDato } returns mellomlagringStart
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)).isTrue

        // true - soknad skal sendes med digisosApi - soknad opprettet etter aktivering
        every { soknadUnderArbeid.opprettetDato } returns mellomlagringStart.plusSeconds(1)
        every { kommuneInfoService.kommuneInfo("1234") } returns KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        assertThat(mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)).isTrue
    }

    @Test
    internal fun getAllVedlegg() {
        // client returnerer null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns null
        assertThat(mellomlagringService.getAllVedlegg("behandlingsId")).isEmpty()

        // mellomlagringMetadataList er null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = null
        )
        assertThat(mellomlagringService.getAllVedlegg("behandlingsId")).isEmpty()

        // mellomlagringMetadataList har innhold
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        val allVedlegg = mellomlagringService.getAllVedlegg("behandlingsId")
        assertThat(allVedlegg).hasSize(1)
        assertThat(allVedlegg[0].filnavn).isEqualTo("filnavn")
    }

    @Test
    internal fun getVedlegg() {
        // client returnerer null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns null
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }

        // mellomlagringMetadataList er null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = null
        )
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }

        // mellomlagringMetadataList har innhold, men finner ikke samme vedleggId
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }

        // mellomlagringMetadataList har innhold og vedleggId finnes
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "vedleggId", storrelse = 123L, mimetype = "mime")
            )
        )
        every { mellomlagringClient.getVedlegg(any(), any()) } returns "hello-world".encodeToByteArray()
        val mellomlagretVedlegg = mellomlagringService.getVedlegg("behandlingsId", "vedleggId")
        assertThat(mellomlagretVedlegg?.data).hasSize("hello-world".length)
        assertThat(mellomlagretVedlegg?.filnavn).isEqualTo("filnavn")
    }
}
