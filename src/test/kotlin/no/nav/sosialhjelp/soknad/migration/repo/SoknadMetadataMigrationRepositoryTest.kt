package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadMetadataMigrationRepositoryTest : RepositoryTest() {

    private var soknadMetadataMigrationRepository: SoknadMetadataMigrationRepository? = null
    private var soknadMetadataRepository: SoknadMetadataRepository? = null

    @BeforeEach
    fun setup() {
        if (soknadMetadataMigrationRepository == null) {
            soknadMetadataMigrationRepository = SoknadMetadataMigrationRepository(jdbcTemplate)
        }
        if (soknadMetadataRepository == null) {
            soknadMetadataRepository = SoknadMetadataRepositoryJdbc(jdbcTemplate)
        }
    }

    @Test
    internal fun `skal hente eldste soknadMetadata etter sistEndretTidspunkt`() {
        val soknadMetadataForGammel = createSoknadMetadata(behandlingsId = "xyz", dagerSiden = 11)
        soknadMetadataRepository!!.opprett(soknadMetadataForGammel)

        val soknadMetadataNeste = createSoknadMetadata(behandlingsId = "123", dagerSiden = 2)
        soknadMetadataRepository!!.opprett(soknadMetadataNeste)

        val soknadMetadataForNy = createSoknadMetadata(behandlingsId = "abc", dagerSiden = 1)
        soknadMetadataRepository!!.opprett(soknadMetadataForNy)

        val result = soknadMetadataMigrationRepository!!.getNextSoknadMetadataAfter(LocalDateTime.now().minusDays(10))

        assertThat(result).isNotNull
        assertThat(result?.behandlingsId).isEqualTo("123")
    }

    @Test
    internal fun `skal returnere null`() {
        val soknadMetadata = createSoknadMetadata(behandlingsId = "123", dagerSiden = 2)
        soknadMetadataRepository!!.opprett(soknadMetadata)

        val result = soknadMetadataMigrationRepository!!.getNextSoknadMetadataAfter(LocalDateTime.now().minusDays(1))

        assertThat(result).isNull()
    }

    @Test
    internal fun `count skal returnere antall`() {
        assertThat(soknadMetadataMigrationRepository!!.count()).isEqualTo(0)

        val soknadMetadata = createSoknadMetadata(behandlingsId = "123", dagerSiden = 2)
        soknadMetadataRepository!!.opprett(soknadMetadata)
        assertThat(soknadMetadataMigrationRepository!!.count()).isEqualTo(1)

        val soknadMetadata2 = createSoknadMetadata(behandlingsId = "456", dagerSiden = 1)
        soknadMetadataRepository!!.opprett(soknadMetadata2)
        assertThat(soknadMetadataMigrationRepository!!.count()).isEqualTo(2)
    }

    private fun createSoknadMetadata(behandlingsId: String, dagerSiden: Long): SoknadMetadata {
        return SoknadMetadata(
            id = soknadMetadataRepository!!.hentNesteId(),
            behandlingsId = behandlingsId,
            fnr = EIER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden),
            lest = false,
        )
    }

    companion object {
        private const val EIER = "eier"
    }
}
