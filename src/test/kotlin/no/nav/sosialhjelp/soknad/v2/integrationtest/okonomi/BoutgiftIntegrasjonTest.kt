package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgifterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarBoutgifterInput
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarIkkeBoutgifterInput
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class BoutgiftIntegrasjonTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Test
    fun `Hente boutgifter skal returnere lagrede data`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)
        okonomiService.addElementToOkonomi(soknad.id, UtgiftType.UTGIFTER_HUSLEIE)
        okonomiService.addElementToOkonomi(soknad.id, UtgiftType.UTGIFTER_STROM)

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = BoutgifterDto::class.java,
        )
            .also {
                assertThat(it.strom).isTrue()
                assertThat(it.husleie).isTrue()
                assertThat(it.boliglan).isFalse()
            }
    }

    @Test
    fun `Ingen boutgifter skal gi bekreftelse false selvom bekreftelse er satt true`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = BoutgifterDto::class.java,
        )
            .also { assertThat(it.bekreftelse).isFalse() }
    }

    @Test
    fun `Ingen bekreftelse returnerer bekreftelse null`() {
        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = BoutgifterDto::class.java,
        )
            .also { assertThat(it.bekreftelse).isNull() }
    }

    @Test
    fun `Oppdatere skal lagres i db`() {
        HarBoutgifterInput(
            hasHusleie = true,
            hasStrom = true,
        ).also {
            doPut(
                uri = getUrl(soknad.id),
                requestBody = it,
                responseBodyClass = BoutgifterDto::class.java,
                soknadId = soknad.id,
            )
        }
        assertThat(okonomiService.getBekreftelser(soknad.id).toList()).hasSize(1)
            .allMatch { it.type == BekreftelseType.BEKREFTELSE_BOUTGIFTER }

        assertThat(okonomiService.getUtgifter(soknad.id).toList()).hasSize(2)
            .anyMatch { it.type == UtgiftType.UTGIFTER_HUSLEIE }
            .anyMatch { it.type == UtgiftType.UTGIFTER_STROM }
    }

    @Test
    fun `Oppdatere med HarIkkeVerdier skal fjerne lagrede`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)
        okonomiService.addElementToOkonomi(soknad.id, UtgiftType.UTGIFTER_HUSLEIE)
        okonomiService.addElementToOkonomi(soknad.id, UtgiftType.UTGIFTER_STROM)

        doPut(
            uri = getUrl(soknad.id),
            requestBody = HarIkkeBoutgifterInput(),
            responseBodyClass = BoutgifterDto::class.java,
            soknadId = soknad.id,
        )
        assertThat(okonomiService.getBekreftelser(soknad.id).toList()).hasSize(1)
            .allMatch { it.type == BekreftelseType.BEKREFTELSE_BOUTGIFTER && !it.verdi }

        assertThat(okonomiService.getUtgifter(soknad.id).toList()).isEmpty()
    }

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(opprettSoknad(id = soknadId))
    }

    companion object {
        private fun getUrl(soknadId: UUID): String {
            return "/soknad/$soknadId/utgifter/boutgifter"
        }
    }
}
