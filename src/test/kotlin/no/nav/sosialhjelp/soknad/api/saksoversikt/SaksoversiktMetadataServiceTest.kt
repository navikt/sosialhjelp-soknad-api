package no.nav.sosialhjelp.soknad.api.saksoversikt

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
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

    private lateinit var soknadMetadata: SoknadMetadata

    @BeforeEach
    fun setUp() {
        val props = mockk<Properties>()
        every { props.getProperty(any()) } returnsArgument 0

        every { clock.zone } returns systemDefault()
        every { clock.instant() } returns LocalDateTime.of(2018, 5, 31, 13, 33, 37).atZone(systemDefault()).toInstant()
        every { navMessageSource.getBundleFor(any(), any()) } returns props

        soknadMetadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "beh123",
            fnr = "12345",
            vedlegg = VedleggMetadataListe(),
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0),
            sistEndretDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0),
            innsendtDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0)
        )

        val v = VedleggMetadata(
            skjema = "skjema1",
            tillegg = "tillegg1",
            status = Vedleggstatus.LastetOpp
        )
        val v2 = VedleggMetadata(
            skjema = "skjema1",
            tillegg = "tillegg1",
            status = Vedleggstatus.LastetOpp,
        )
        val v3 = VedleggMetadata(
            skjema = "skjema2",
            tillegg = "tillegg1",
            status = Vedleggstatus.VedleggKreves
        )
        val v4 = VedleggMetadata(
            skjema = "annet",
            tillegg = "annet",
            status = Vedleggstatus.VedleggKreves
        )

        soknadMetadata.vedlegg?.vedleggListe?.add(v)
        soknadMetadata.vedlegg?.vedleggListe?.add(v2)
        soknadMetadata.vedlegg?.vedleggListe?.add(v3)
        soknadMetadata.vedlegg?.vedleggListe?.add(v4)
    }

    @Test
    fun henterInnsendteForBruker() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns listOf(soknadMetadata)

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
        } returns listOf(soknadMetadata)
        every { ettersendingService.hentNyesteSoknadIKjede(any()) } returns soknadMetadata

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
        } returns listOf(soknadMetadata)
        every { ettersendingService.hentNyesteSoknadIKjede(any()) } returns soknadMetadata

        saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345")

        assertThat(timeSlot.captured)
            .isEqualTo(LocalDateTime.of(2018, 5, 31, 13, 33, 37).minusDays(ETTERSENDELSE_FRIST_DAGER.toLong()))
    }
}
