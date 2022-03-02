package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.Arrays
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class BatchSoknadMetadataRepositoryJdbcTest {
    private val dagerGammelSoknad = 20
    private val behandlingsId = "1100AAAAA"

    @Inject
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository? = null

    @Inject
    private val soknadMetadataRepository: SoknadMetadataRepository? = null

    @Inject
    private val support: RepositoryTestSupport? = null

    @AfterEach
    fun teardown() {
        support!!.jdbcTemplate.update("DELETE FROM soknadmetadata WHERE behandlingsid = ?", behandlingsId)
    }

    @Test
    fun hentForBatchSkalIkkeReturnereFerdige() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.FERDIG, dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNotPresent
    }

    @Test
    fun hentForBatchSkalIkkeReturnereAvbruttAutomatisk() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId,
            SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK,
            dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNotPresent
    }

    @Test
    fun hentForBatchSkalIkkeReturnereAvbruttAvBruker() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId,
            SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER,
            dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isNotPresent
    }

    @Test
    fun hentForBatchBrukerEndringstidspunkt() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId,
            SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            dagerGammelSoknad))
        assertThat(batchSoknadMetadataRepository!!.hentForBatch(dagerGammelSoknad - 1)).isPresent
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad + 1)).isNotPresent
    }

    @Test
    fun hentEldreEnnBrukerEndringstidspunktUavhengigAvStatus() {
        val statuser = Arrays.asList(SoknadMetadataInnsendingStatus.UNDER_ARBEID, SoknadMetadataInnsendingStatus.FERDIG,
            SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK, SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER)
        for (status in statuser) {
            opprettSoknadMetadata(soknadMetadata(behandlingsId, status, dagerGammelSoknad))
            assertThat(batchSoknadMetadataRepository!!.hentEldreEnn(dagerGammelSoknad - 1)).isPresent
            assertThat(batchSoknadMetadataRepository.hentEldreEnn(dagerGammelSoknad + 1)).isNotPresent
            batchSoknadMetadataRepository.slettSoknadMetaData(behandlingsId)
        }
    }

    private fun opprettSoknadMetadata(soknadMetadata: SoknadMetadata) {
        soknadMetadataRepository!!.opprett(soknadMetadata)
        val lagretSoknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId)
        batchSoknadMetadataRepository!!.leggTilbakeBatch(lagretSoknadMetadata!!.id)
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int,
    ): SoknadMetadata {
        val meta = SoknadMetadata()
        meta.id = soknadMetadataRepository!!.hentNesteId()
        meta.behandlingsId = behandlingsId
        meta.fnr = EIER
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL
        meta.skjema = ""
        meta.status = status
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.lestDittNav = false
        return meta
    }

    companion object {
        private const val EIER = "11111111111"
    }
}
