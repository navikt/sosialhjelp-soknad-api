package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.app.exceptions.SoknadUnderArbeidIkkeFunnetException
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = [SlettGamleSoknaderDbTest.PropertyInitializer::class])
@Transactional
@ActiveProfiles("no-redis", "test", "test-container")
class SlettGamleSoknaderDbTest {
    @Autowired
    private lateinit var scheduler: SlettSoknadUnderArbeidScheduler

    @Autowired
    private lateinit var metadataRepo: SoknadMetadataRepository

    @Autowired
    private lateinit var soknadUnderArbeidRepo: SoknadUnderArbeidRepository

    @Autowired
    private lateinit var ctx: ConfigurableApplicationContext

    @BeforeEach
    fun setup() {
        TestPropertyValues.of(
            "sendsoknad.batch.enabled=true",
            "scheduler.disable=false",
        ).applyTo(ctx.environment)
    }

    @Test
    fun `Scheduler skal slette soknadsdata og oppdatere metadata`() {
        val gammelId = lagGammelSoknad()
        val nyId = lagNySoknad()

        assertSoknaderFinnes(listOf(gammelId, nyId))
        assertMetadataFinnes(listOf(gammelId, nyId))

        scheduler.slettGamleSoknadUnderArbeid()

        assertSoknaderFinnes(listOf(nyId))
        assertSoknadSlettetOgMetadataEndretStatus(listOf(gammelId))
    }

    @Test
    fun `Liste med pabegynte soknader skal endres etter sletting`() {
        val gammelId = lagGammelSoknad()
        val nyId = lagNySoknad()

        assertSoknaderFinnes(listOf(gammelId, nyId))
        assertMetadataFinnes(listOf(gammelId, nyId))

        val pabegynteForSletting =
            metadataRepo.hentPabegynteSoknaderForBruker(EIER).also { soknader ->
                assertThat(soknader).hasSize(2)
                assertSoknaderFinnes(soknader.map { it.behandlingsId })
            }

        scheduler.slettGamleSoknadUnderArbeid()

        pabegynteForSletting
            .map { metadata -> metadataRepo.hent(metadata.behandlingsId)!! }
            .forEach { metadata ->
                when (metadata.status) {
                    SoknadMetadataInnsendingStatus.UNDER_ARBEID -> assertSoknaderFinnes(listOf(metadata.behandlingsId))
                    SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK -> assertSoknadSlettetOgMetadataEndretStatus(listOf(metadata.behandlingsId))
                    else -> error("Skal ikke skje")
                }
            }
    }

    private fun assertSoknaderFinnes(soknadIds: List<String>) {
        soknadIds.forEach {
            soknadUnderArbeidRepo.hentSoknad(it, EIER).let { soknad ->
                assertThat(soknad.status).isEqualTo(SoknadUnderArbeidStatus.UNDER_ARBEID)
            }
        }
    }

    private fun assertMetadataFinnes(soknadIds: List<String>) {
        soknadIds.forEach {
            metadataRepo.hent(it)!!.let { metadata ->
                assertThat(metadata.status).isEqualTo(SoknadMetadataInnsendingStatus.UNDER_ARBEID)
            }
        }
    }

    private fun assertSoknadSlettetOgMetadataEndretStatus(soknadIds: List<String>) {
        soknadIds.forEach {
            assertThatThrownBy {
                soknadUnderArbeidRepo.hentSoknad(it, EIER)
            }.isInstanceOf(SoknadUnderArbeidIkkeFunnetException::class.java)

            assertThat(metadataRepo.hent(it)!!.status)
                .isEqualTo(SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK)
        }
    }

    private fun lagGammelSoknad(): String {
        return UUID.randomUUID().toString()
            .also { behandlingsId ->

                SoknadMetadata(
                    id = 1L,
                    behandlingsId = behandlingsId,
                    fnr = EIER,
                    skjema = "",
                    type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
                    status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
                    opprettetDato = FOR_GAMMEL,
                    sistEndretDato = FOR_GAMMEL,
                    innsendtDato = null,
                    kortSoknad = false,
                ).let { metadataRepo.opprett(it) }

                SoknadUnderArbeid(
                    soknadId = 1L,
                    versjon = 1L,
                    behandlingsId = behandlingsId,
                    eier = EIER,
                    jsonInternalSoknad = SoknadServiceOld.createEmptyJsonInternalSoknad(behandlingsId, false),
                    status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                    opprettetDato = FOR_GAMMEL,
                    sistEndretDato = FOR_GAMMEL,
                ).also { soknadUnderArbeidRepo.opprettSoknad(it, it.eier) }
            }
    }

    fun lagNySoknad(): String {
        return UUID.randomUUID().toString()
            .also { behandlingsId ->

                SoknadMetadata(
                    id = 2L,
                    behandlingsId = behandlingsId,
                    fnr = EIER,
                    skjema = "",
                    type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
                    status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
                    opprettetDato = NY_ISH,
                    sistEndretDato = NY_ISH,
                    innsendtDato = null,
                    kortSoknad = false,
                ).also { metadataRepo.opprett(it) }

                SoknadUnderArbeid(
                    soknadId = 2L,
                    versjon = 2L,
                    behandlingsId = behandlingsId,
                    eier = EIER,
                    jsonInternalSoknad = SoknadServiceOld.createEmptyJsonInternalSoknad(behandlingsId, false),
                    status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                    opprettetDato = NY_ISH,
                    sistEndretDato = NY_ISH,
                ).also { soknadUnderArbeidRepo.opprettSoknad(it, it.eier) }
            }
    }

    class PropertyInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "sendsoknad.batch.enabled=true",
                "scheduler.disable=false",
            ).applyTo(applicationContext.environment)
        }
    }

    companion object {
        private val FOR_GAMMEL = LocalDateTime.now().minusDays(15)
        private val NY_ISH = LocalDateTime.now().minusHours(1)
        private val EIER = "01020312345"
    }
}
