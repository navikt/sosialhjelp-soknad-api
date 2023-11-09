package no.nav.sosialhjelp.soknad.repository.soknadmetadata

import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class BatchSoknadMetadataRepositoryJdbcTest : RepositoryTest() {
    private val dagerGammelSoknad = 20
    private val behandlingsId = "1100AAAAA"

    private var batchSoknadMetadataRepository: BatchSoknadMetadataRepository? = null
    private var soknadMetadataRepository: SoknadMetadataRepository? = null

    @BeforeEach
    fun setup() {
        if (batchSoknadMetadataRepository == null) {
            batchSoknadMetadataRepository = BatchSoknadMetadataRepositoryJdbc(jdbcTemplate, namedParameterJdbcTemplate)
        }
        if (soknadMetadataRepository == null) {
            soknadMetadataRepository = SoknadMetadataRepositoryJdbc(jdbcTemplate)
        }
    }

    @Test
    fun hentForBatchSkalIkkeReturnereFerdige() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, FERDIG, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNull()
    }

    @Test
    fun hentForBatchSkalIkkeReturnereAvbruttAutomatisk() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, AVBRUTT_AUTOMATISK, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNull()
    }

    @Test
    fun hentForBatchSkalIkkeReturnereAvbruttAvBruker() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, AVBRUTT_AV_BRUKER, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNull()
    }

    @Test
    fun hentForBatchBrukerEndringstidspunkt() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, UNDER_ARBEID, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNotNull
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad + 1)).isNull()
    }

    @Test
    fun hentEldreEnnBrukerEndringstidspunktUavhengigAvStatus() {
        for (status in listOf(UNDER_ARBEID, FERDIG, AVBRUTT_AUTOMATISK, AVBRUTT_AV_BRUKER)) {
            opprettSoknadMetadata(soknadMetadata(behandlingsId, status, dagerGammelSoknad))
            assertThat(batchSoknadMetadataRepository!!.hentEldreEnn(dagerGammelSoknad - 1)).isNotEmpty
            assertThat(batchSoknadMetadataRepository!!.hentEldreEnn(dagerGammelSoknad + 1)).isEmpty()
            batchSoknadMetadataRepository!!.slettSoknadMetaDataer(listOf(behandlingsId))
        }
    }

    @Test
    internal fun `hentEldreEnn skal hente 20 siste`() {
        // oppretter noen SoknadMetadata som er nyere enn `antallDagerGammelt`
        opprettSoknadMetadata(soknadMetadata(behandlingsId + "A", FERDIG, dagerGammelSoknad - 2))
        opprettSoknadMetadata(soknadMetadata(behandlingsId + "B", FERDIG, dagerGammelSoknad - 1))

        // oppretter over 20 SoknadMetadata som er eldre enn `antallDagerGammelt`
        (0..22).forEach {
            opprettSoknadMetadata(soknadMetadata(behandlingsId + it, FERDIG, dagerGammelSoknad + it))
        }

        val bolk = batchSoknadMetadataRepository!!.hentEldreEnn(dagerGammelSoknad)
        assertThat(bolk).hasSize(20)
        bolk.forEachIndexed { i, soknadMetadata ->
            assertThat(soknadMetadata.behandlingsId).isEqualTo(behandlingsId + i)
        }
    }

    private fun opprettSoknadMetadata(soknadMetadata: SoknadMetadata) {
        soknadMetadataRepository!!.opprett(soknadMetadata)
        val lagretSoknadMetadata = soknadMetadataRepository!!.hent(soknadMetadata.behandlingsId)
        batchSoknadMetadataRepository!!.leggTilbakeBatch(lagretSoknadMetadata!!.id!!)
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int,
    ): SoknadMetadata {
        return SoknadMetadata(
            id = soknadMetadataRepository!!.hentNesteId(),
            behandlingsId = behandlingsId,
            fnr = EIER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = status,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            lest = false,
        )
    }

    companion object {
        private const val EIER = "11111111111"
    }
}
