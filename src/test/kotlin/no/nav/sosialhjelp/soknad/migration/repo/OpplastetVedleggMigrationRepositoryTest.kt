package no.nav.sosialhjelp.soknad.migration.repo

import jakarta.inject.Inject
import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import org.apache.commons.lang3.RandomUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [Application::class])
@ActiveProfiles(profiles = ["no-redis", "test"])
internal class OpplastetVedleggMigrationRepositoryTest {

    @Inject
    private lateinit var opplastetVedleggMigrationRepository: OpplastetVedleggMigrationRepository

    @Inject
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
    }

    @Test
    internal fun `skal hente alle vedlegg for id`() {
        val vedlegg = createVedlegg(soknadId = 1)
        opplastetVedleggRepository.opprettVedlegg(vedlegg, EIER)

        val vedlegg2 = createVedlegg(soknadId = 1)
        opplastetVedleggRepository.opprettVedlegg(vedlegg2, EIER)

        val vedleggAnnenSoknad = createVedlegg(soknadId = 2)
        opplastetVedleggRepository.opprettVedlegg(vedleggAnnenSoknad, EIER)

        val vedleggList = opplastetVedleggMigrationRepository.getOpplastetVedlegg(1)

        assertThat(vedleggList).hasSize(2)
    }

    @Test
    internal fun `skal returnere tom liste`() {
        val vedlegg = createVedlegg(soknadId = 1)
        opplastetVedleggRepository.opprettVedlegg(vedlegg, EIER)

        val vedleggList = opplastetVedleggMigrationRepository.getOpplastetVedlegg(2)

        assertThat(vedleggList).isEmpty()
    }

    @Test
    internal fun `count skal returnere antall`() {
        assertThat(opplastetVedleggMigrationRepository.count()).isEqualTo(0)

        val vedlegg = createVedlegg(soknadId = 1)
        opplastetVedleggRepository.opprettVedlegg(vedlegg, EIER)
        assertThat(opplastetVedleggMigrationRepository.count()).isEqualTo(1)

        val vedlegg2 = createVedlegg(soknadId = 2)
        opplastetVedleggRepository.opprettVedlegg(vedlegg2, EIER)
        assertThat(opplastetVedleggMigrationRepository.count()).isEqualTo(2)
    }

    companion object {
        private const val EIER = "eier"

        private fun createVedlegg(eier: String = EIER, soknadId: Long): OpplastetVedlegg {
            return OpplastetVedlegg(
                eier = eier,
                vedleggType = OpplastetVedleggType("bostotte|annetboutgift"),
                data = RandomUtils.nextBytes(4),
                soknadId = soknadId,
                filnavn = "filnavn",
                sha512 = "sha"
            )
        }
    }
}
