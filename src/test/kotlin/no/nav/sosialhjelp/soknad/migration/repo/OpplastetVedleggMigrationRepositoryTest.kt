package no.nav.sosialhjelp.soknad.migration.repo

import jakarta.inject.Inject
import no.nav.sosialhjelp.soknad.TestApplication
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class OpplastetVedleggMigrationRepositoryTest {

    @Inject
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Inject
    private lateinit var opplastetVedleggMigrationRepository: OpplastetVedleggMigrationRepository

    @Inject
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    internal fun `skal hente alle vedlegg for id`() {
        val id1 = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid("behandlingsId"), EIER)!!
        val id2 = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid("behandlingsId2"), EIER)!!

        val vedlegg = createVedlegg(soknadId = id1)
        opplastetVedleggRepository.opprettVedlegg(vedlegg, EIER)

        val vedlegg2 = createVedlegg(soknadId = id1)
        opplastetVedleggRepository.opprettVedlegg(vedlegg2, EIER)

        val vedleggAnnenSoknad = createVedlegg(soknadId = id2)
        opplastetVedleggRepository.opprettVedlegg(vedleggAnnenSoknad, EIER)

        val vedleggList = opplastetVedleggMigrationRepository.getOpplastetVedlegg(id1)

        assertThat(vedleggList).hasSize(2)
    }

    @Test
    internal fun `skal returnere tom liste`() {
        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid("behandlingsId"), EIER)!!

        val vedlegg = createVedlegg(soknadId = soknadId)
        opplastetVedleggRepository.opprettVedlegg(vedlegg, EIER)

        val vedleggList = opplastetVedleggMigrationRepository.getOpplastetVedlegg(soknadUnderArbeidId = 123L)

        assertThat(vedleggList).isEmpty()
    }

    @Test
    internal fun `count skal returnere antall`() {
        assertThat(opplastetVedleggMigrationRepository.count()).isEqualTo(0)

        val soknadId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid("behandlingsId"), EIER)!!
        val vedlegg = createVedlegg(soknadId = soknadId)
        opplastetVedleggRepository.opprettVedlegg(vedlegg, EIER)
        assertThat(opplastetVedleggMigrationRepository.count()).isEqualTo(1)

        val vedlegg2 = createVedlegg(soknadId = soknadId)
        opplastetVedleggRepository.opprettVedlegg(vedlegg2, EIER)
        assertThat(opplastetVedleggMigrationRepository.count()).isEqualTo(2)
    }

    companion object {
        private const val EIER = "eier"

        private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = behandlingsId,
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = null,
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }

        private fun createVedlegg(eier: String = EIER, soknadId: Long): OpplastetVedlegg {
            return OpplastetVedlegg(
                eier = eier,
                vedleggType = OpplastetVedleggType("bostotte|annetboutgift"),
                data = byteArrayOf(1, 2, 3, 4),
                soknadId = soknadId,
                filnavn = "filnavn",
                sha512 = "sha"
            )
        }
    }
}
