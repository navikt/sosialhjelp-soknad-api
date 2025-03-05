package no.nav.sosialhjelp.soknad.scheduled

import no.nav.sosialhjelp.soknad.app.exceptions.SoknadUnderArbeidIkkeFunnetException
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.v2.json.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions
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
@ContextConfiguration(initializers = [AvbrytSoknaderDbTest.PropertyInitializer::class])
@Transactional
@ActiveProfiles("no-redis", "test", "test-container")
class AvbrytSoknaderDbTest {
    @Autowired
    private lateinit var scheduler: AvbrytAutomatiskScheduler

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
    fun `Scheduler skal oppdatere metadata og slette soknadsdata`() {
        val gammelId = lagGammelSoknad()
        val nyId = lagNySoknad()

        assertSoknaderFinnes(listOf(gammelId, nyId))
        assertMetadataFinnes(listOf(gammelId, nyId))

        scheduler.avbrytGamleSoknader()

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
                Assertions.assertThat(soknader).hasSize(2)
                assertSoknaderFinnes(soknader.map { it.behandlingsId })
            }

        scheduler.avbrytGamleSoknader()

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

    @Test
    fun `Batch-sletting av mange soknader skal kjore flere runder`() {
        val gamleSoknadIds = lagMangeGamleSoknader()
        val nyId = lagNySoknad(id = 0L)

        assertSoknaderFinnes(gamleSoknadIds.plus(nyId))
        assertMetadataFinnes(gamleSoknadIds.plus(nyId))

        scheduler.avbrytGamleSoknader()

        assertSoknaderFinnes(listOf(nyId))
        assertSoknadSlettetOgMetadataEndretStatus(gamleSoknadIds)
    }

    private fun assertSoknaderFinnes(soknadIds: List<String>) {
        soknadIds.forEach {
            soknadUnderArbeidRepo.hentSoknad(it, EIER).let { soknad ->
                Assertions.assertThat(soknad.status).isEqualTo(SoknadUnderArbeidStatus.UNDER_ARBEID)
            }
        }
    }

    private fun assertMetadataFinnes(soknadIds: List<String>) {
        soknadIds.forEach {
            metadataRepo.hent(it)!!.let { metadata ->
                Assertions.assertThat(metadata.status).isEqualTo(SoknadMetadataInnsendingStatus.UNDER_ARBEID)
            }
        }
    }

    private fun assertSoknadSlettetOgMetadataEndretStatus(soknadIds: List<String>) {
        soknadIds.forEach {
            Assertions.assertThatThrownBy {
                soknadUnderArbeidRepo.hentSoknad(it, EIER)
            }.isInstanceOf(SoknadUnderArbeidIkkeFunnetException::class.java)

            Assertions.assertThat(metadataRepo.hent(it)!!.status)
                .isEqualTo(SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK)
        }
    }

    private fun lagMangeGamleSoknader(): List<String> {
        return mutableListOf<String>().apply {
            for (i in 1L..250L) {
                add(lagGammelSoknad(id = i))
            }
        }
    }

    private fun lagGammelSoknad(
        id: Long = 1L,
        behandlingsId: String = UUID.randomUUID().toString(),
    ): String {
        SoknadMetadata(
            id = id,
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
            soknadId = id,
            versjon = 1L,
            behandlingsId = behandlingsId,
            eier = EIER,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(behandlingsId, false),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = FOR_GAMMEL,
            sistEndretDato = FOR_GAMMEL,
        ).also { soknadUnderArbeidRepo.opprettSoknad(it, it.eier) }

        return behandlingsId
    }

    fun lagNySoknad(id: Long = 2L): String {
        return UUID.randomUUID().toString()
            .also { behandlingsId ->

                SoknadMetadata(
                    id = id,
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
                    soknadId = id,
                    versjon = 1L,
                    behandlingsId = behandlingsId,
                    eier = EIER,
                    jsonInternalSoknad = createEmptyJsonInternalSoknad(behandlingsId, false),
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
