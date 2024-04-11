package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("no-redis", "test", "test-container")
internal class BatchSoknadMetadataRepositoryJdbcTest {
    private val dagerGammelSoknad = 20
    private val behandlingsId get() = UUID.randomUUID().toString()

    @Autowired
    private lateinit var batchSoknadMetadataRepository: BatchSoknadMetadataRepository

    @Autowired
    private lateinit var soknadMetadataRepository: SoknadMetadataRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun teardown() {
        jdbcTemplate.update("DELETE FROM soknadmetadata")
    }

    @Test
    fun hentForBatchSkalIkkeReturnereFerdige() {
        opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, FERDIG, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNull()
    }

    @Test
    fun hentForBatchSkalIkkeReturnereAvbruttAutomatisk() {
        opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, AVBRUTT_AUTOMATISK, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNull()
    }

    @Test
    fun hentForBatchSkalIkkeReturnereAvbruttAvBruker() {
        opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, AVBRUTT_AV_BRUKER, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNull()
    }

    @Test
    fun hentForBatchBrukerEndringstidspunkt() {
        opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, UNDER_ARBEID, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNotNull
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad + 1)).isNull()
    }

    @Test
    fun hentEldreEnnBrukerEndringstidspunktUavhengigAvStatus() {
        for (status in listOf(UNDER_ARBEID, FERDIG, AVBRUTT_AUTOMATISK, AVBRUTT_AV_BRUKER)) {
            opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, status, dagerGammelSoknad))
            assertThat(batchSoknadMetadataRepository.hentEldreEnn(dagerGammelSoknad - 1)).isNotEmpty
            assertThat(batchSoknadMetadataRepository.hentEldreEnn(dagerGammelSoknad + 1)).isEmpty()
            batchSoknadMetadataRepository.slettSoknadMetaDataer(listOf(behandlingsId))
        }
    }

    @Test
    internal fun `hentEldreEnn skal hente 20 siste`() {
        // oppretter noen SoknadMetadata som er nyere enn `antallDagerGammelt`
        opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, FERDIG, dagerGammelSoknad - 2))
        opprettSoknadMetadata(lagSoknadMetadata(behandlingsId, FERDIG, dagerGammelSoknad - 1))

        // oppretter over 20 SoknadMetadata som er eldre enn `antallDagerGammelt`
        val oldSoknads = (0..22).map {
            behandlingsId.also { id ->
                opprettSoknadMetadata(lagSoknadMetadata(id, FERDIG, dagerGammelSoknad + it))
            }
        }

        val bolk = batchSoknadMetadataRepository.hentEldreEnn(dagerGammelSoknad)
        assertThat(bolk).hasSize(20)
        bolk.forEach {
            assertThat(oldSoknads).contains(it.behandlingsId)
        }
    }

    private fun opprettSoknadMetadata(soknadMetadata: SoknadMetadata) {
        soknadMetadataRepository.opprett(soknadMetadata)
        val lagretSoknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId)
        batchSoknadMetadataRepository.leggTilbakeBatch(lagretSoknadMetadata!!.id)
    }

    private fun lagSoknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int
    ): SoknadMetadata {
        return SoknadMetadata(
            behandlingsId = behandlingsId,
            idGammeltFormat = behandlingsId,
            fnr = EIER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = status,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            lest = false
        )
    }

    companion object {
        private const val EIER = "11111111111"
    }
}
