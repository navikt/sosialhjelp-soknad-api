package no.nav.sosialhjelp.soknad.api.saksoversikt

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataType
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService.Companion.ETTERSENDELSE_FRIST_DAGER
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId.systemDefault
import java.util.Properties

internal class SaksoversiktMetadataServiceTest {

    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val ettersendingService: EttersendingService = mockk()
    private val navMessageSource: NavMessageSource = mockk()
    private val clock: Clock = mockk()

    private val saksoversiktMetadataService = SaksoversiktMetadataService(
        soknadMetadataRepository,
        ettersendingService,
        navMessageSource,
        clock
    )

    private var soknadMetadata: SoknadMetadata? = null

    @BeforeEach
    fun setUp() {
        val props = mockk<Properties>()
        every { props.getProperty(any()) } returnsArgument 0

        every { clock.zone } returns systemDefault()
        every { clock.instant() } returns LocalDateTime.of(2018, 5, 31, 13, 33, 37).atZone(systemDefault()).toInstant()
        every { navMessageSource.getBundleFor(any(), any()) } returns props

        soknadMetadata = SoknadMetadata()
        soknadMetadata!!.fnr = "12345"
        soknadMetadata!!.behandlingsId = "beh123"
        soknadMetadata!!.type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL
        soknadMetadata!!.innsendtDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0)

        val v = VedleggMetadata()
        v.skjema = "skjema1"
        v.tillegg = "tillegg1"
        v.status = Vedleggstatus.LastetOpp
        val v2 = VedleggMetadata()
        v2.skjema = "skjema1"
        v2.tillegg = "tillegg1"
        v2.status = Vedleggstatus.LastetOpp
        val v3 = VedleggMetadata()
        v3.skjema = "skjema2"
        v3.tillegg = "tillegg1"
        v3.status = Vedleggstatus.VedleggKreves
        val v4 = VedleggMetadata()
        v4.skjema = "annet"
        v4.tillegg = "annet"
        v4.status = Vedleggstatus.VedleggKreves

        val vedleggListe = soknadMetadata!!.vedlegg.vedleggListe
        vedleggListe.add(v)
        vedleggListe.add(v2)
        vedleggListe.add(v3)
        vedleggListe.add(v4)
    }

    @Test
    fun henterInnsendteForBruker() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns listOf(soknadMetadata!!)

        val resultat = saksoversiktMetadataService.hentInnsendteSoknaderForFnr("12345")

        assertThat(resultat).hasSize(1)
        val soknad = resultat[0]
        assertThat(soknad.behandlingsId).isEqualTo("beh123")
        assertThat(soknad.hoveddokument.tittel).isEqualTo("saksoversikt.soknadsnavn")
        assertThat(soknad.vedlegg).hasSize(1)
        assertThat(soknad.vedlegg[0].tittel).isEqualTo("vedlegg.skjema1.tillegg1.tittel")
        assertThat(soknad.innsendtDato).isEqualTo(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-04-11 13:30:00"))
    }

    @Test
    fun hentForEttersendelse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns listOf(soknadMetadata!!)
        every { ettersendingService.hentNyesteSoknadIKjede(any()) } returns soknadMetadata!!

        val resultat = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345")

        assertThat(resultat).hasSize(1)
        val soknad = resultat[0]
        assertThat(soknad.tittel).contains("saksoversikt.soknadsnavn")
        assertThat(soknad.vedlegg).hasSize(1)
        assertThat(soknad.vedlegg[0].tittel).isEqualTo("vedlegg.skjema2.tillegg1.tittel")
    }

    @Test
    fun hentForEttersendelseHarRiktigInterval() {
        val timeSlot = slot<LocalDateTime>()
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), capture(timeSlot))
        } returns listOf(soknadMetadata!!)
        every { ettersendingService.hentNyesteSoknadIKjede(any()) } returns soknadMetadata!!

        saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345")

        assertThat(timeSlot.captured)
            .isEqualTo(LocalDateTime.of(2018, 5, 31, 13, 33, 37).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong()))
    }
}
