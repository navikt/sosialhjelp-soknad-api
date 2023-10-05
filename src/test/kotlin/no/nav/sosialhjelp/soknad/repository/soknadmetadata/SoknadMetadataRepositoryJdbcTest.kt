package no.nav.sosialhjelp.soknad.repository.soknadmetadata

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadMetadataRepositoryJdbcTest : RepositoryTest() {

    private val behandlingsId = "1100AAAAA"

    private var soknadMetadataRepository: SoknadMetadataRepository? = null

    @BeforeEach
    fun setup() {
        if (soknadMetadataRepository == null) {
            soknadMetadataRepository = SoknadMetadataRepositoryJdbc(jdbcTemplate)
        }
    }

    @Test
    fun oppdaterLest() {
        var soknadMetadata = soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.UNDER_ARBEID, 12)
        assertThat(soknadMetadata.lest).isFalse
        soknadMetadataRepository!!.opprett(soknadMetadata)

        soknadMetadata = soknadMetadataRepository!!.hent(soknadMetadata.behandlingsId)!!
        soknadMetadata.lest = true

        soknadMetadataRepository!!.oppdaterLest(soknadMetadata, EIER)

        val soknadMetadataFraDb = soknadMetadataRepository!!.hent(behandlingsId)
        assertThat(soknadMetadataFraDb).isNotNull
        assertThat(soknadMetadataFraDb?.lest).isTrue
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
