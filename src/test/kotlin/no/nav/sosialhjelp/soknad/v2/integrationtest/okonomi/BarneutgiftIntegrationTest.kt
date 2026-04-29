package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.createBarn
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.familie.service.FamilieRegisterService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BarneutgifterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarBarneutgifterInput
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BarneutgiftIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Autowired
    private lateinit var familieRegisterService: FamilieRegisterService

    @Autowired
    private lateinit var dokRepository: DokumentasjonRepository

    @Test
    fun `Hente barneutgifter skal returnere lagrede data`() {
        setForsorgerplikt(true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BARNEUTGIFTER, verdi = true)
        okonomiService.addElementToOkonomi(soknad.id, UtgiftType.UTGIFTER_BARNEHAGE)
        okonomiService.addElementToOkonomi(soknad.id, UtgiftType.UTGIFTER_SFO)

        doGet(
            uri = getUrl(),
            responseBodyClass = BarneutgifterDto::class.java,
        )
            .also {
                assertThat(it.hasForsorgerplikt).isTrue()
                assertThat(it.hasBekreftelse).isTrue()
                assertThat(it.hasBarnehage).isTrue()
                assertThat(it.hasSfo).isTrue()
                assertThat(it.hasAnnenUtgiftBarn).isFalse()
            }
    }

    @Test
    fun `Ingen bekreftelse returnerer bekreftelse null`() {
        setForsorgerplikt(true)

        doGet(
            uri = getUrl(),
            responseBodyClass = BarneutgifterDto::class.java,
        )
            .also {
                assertThat(it.hasForsorgerplikt).isTrue()
                assertThat(it.hasBekreftelse).isNull()
            }
    }

    @Test
    fun `Oppdatere skal lagre data i db`() {
        setForsorgerplikt(true)

        doPut(
            uri = getUrl(),
            requestBody = HarBarneutgifterInput(hasBarnehage = true, hasSfo = true),
            responseBodyClass = BarneutgifterDto::class.java,
            soknadId = soknad.id,
        )

        assertThat(okonomiService.getBekreftelser(soknad.id).toList()).hasSize(1)
            .allMatch { it.type == BekreftelseType.BEKREFTELSE_BARNEUTGIFTER }
        assertThat(okonomiService.getUtgifter(soknad.id).toList()).hasSize(2)
            .anyMatch { it.type == UtgiftType.UTGIFTER_BARNEHAGE }
            .anyMatch { it.type == UtgiftType.UTGIFTER_SFO }

        assertThat(dokRepository.findAllBySoknadId(soknad.id)).hasSize(2)
            .anyMatch { it.type == UtgiftType.UTGIFTER_BARNEHAGE }
            .anyMatch { it.type == UtgiftType.UTGIFTER_SFO }
    }

    @Test
    fun `Forsorgerplikt null skal returnere object med forsorgerplikt false og bekreftelse null`() {
        doGet(
            uri = getUrl(),
            responseBodyClass = BarneutgifterDto::class.java,
        )
            .also {
                assertThat(it.hasForsorgerplikt).isFalse()
                assertThat(it.hasBekreftelse).isNull()
            }
    }

    @Test
    fun `Forsorgerplikt false skal returnere object med forsorgerplikt false og bekreftelse null`() {
        setForsorgerplikt(false)

        doGet(
            uri = getUrl(),
            responseBodyClass = BarneutgifterDto::class.java,
        )
            .also {
                assertThat(it.hasForsorgerplikt).isFalse()
                assertThat(it.hasBekreftelse).isNull()
            }
    }

    @Test
    fun `Oppdatere med Forsorgerplikt null skal ikke lagres i db`() {
        doPut(
            uri = getUrl(),
            requestBody = HarBarneutgifterInput(hasBarnehage = true, hasSfo = true),
            responseBodyClass = BarneutgifterDto::class.java,
            soknadId = soknad.id,
        )

        assertThat(okonomiService.getBekreftelser(soknad.id)).isEmpty()
        assertThat(okonomiService.getUtgifter(soknad.id)).isEmpty()
    }

    @Test
    fun `Oppdatere med Forsorgerplikt false skal ikke lagres i db`() {
        setForsorgerplikt(false)

        doPut(
            uri = getUrl(),
            requestBody = HarBarneutgifterInput(hasBarnehage = true, hasSfo = true),
            responseBodyClass = BarneutgifterDto::class.java,
            soknadId = soknad.id,
        )

        assertThat(okonomiService.getBekreftelser(soknad.id)).isEmpty()
        assertThat(okonomiService.getUtgifter(soknad.id)).isEmpty()
    }

    private fun setForsorgerplikt(value: Boolean) {
        familieRegisterService.updateForsorgerpliktRegister(
            soknadId = soknad.id,
            harForsorgerplikt = value,
            barn = listOf(createBarn()),
        )
    }

    private lateinit var soknad: Soknad

    private fun getUrl(): String {
        return "/soknad/${soknad.id}/utgifter/barneutgifter"
    }

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(opprettSoknad(id = soknadId))
    }
}
