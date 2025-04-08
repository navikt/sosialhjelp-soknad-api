package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgifterDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class BostotteInfoUseCaseTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Test
    fun `Har hverken bostotte eller boutgifter skal vise false`() {
        assertSkalVise(false)
    }

    @Test
    fun `Svart nei bostotte og ingen boutgifter skal gi false`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = false)
        assertSkalVise(false)
    }

    @Test
    fun `Har boutgifter og ikke bostotte skal vise`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)
        assertSkalVise(true)
    }

    @Test
    fun `Har bostotte og samtykke, men ingen boutgifter`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)

        assertSkalVise(false)
    }

    @Test
    fun `Finnes boutgifter og bostotte skal ikke vise`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)

        assertSkalVise(false)
    }

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(opprettSoknad(id = soknadId))
    }

    private fun assertSkalVise(vise: Boolean) {
        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = BoutgifterDto::class.java,
        )
            .also { dto -> assertThat(dto.skalViseInfoVedBekreftelse == vise).isTrue() }
    }

    companion object {
        private fun getUrl(soknadId: UUID): String {
            return "/soknad/$soknadId/utgifter/boutgifter"
        }
    }
}
