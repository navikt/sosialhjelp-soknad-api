package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.createBostotteSak
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgifterDto
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Integrasjonstatus
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

// TODO Er ikke kjempeglad i at denne logikken eies og testes av backend
class BostotteInfoUseCaseTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Autowired
    private lateinit var integrasjonstatusRepository: IntegrasjonstatusRepository

    // TODO Klarer ikke helt å se hvilken innvirkning innhenting av bostotte feilet har på dette...
    // TODO Det kan ikke feile, uten at BOSTOTTE == true og BOSTOTTE_SAMTYKKE == true ->
    // TODO ...og på det tidspunktet er det ikke relevant om den har feilet eller ei?
    @Test
    fun `Hente bostotte feilet-scenario`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)
        integrasjonstatusRepository.save(Integrasjonstatus(soknad.id, feilStotteHusbanken = true))

        assertSkalVise(false)
    }

    @Test
    fun `Mangler samtykke-scenario og har ingen relevante boutgift-bekreftelser`() {
        assertSkalVise(false)
    }

    @Test
    fun `Mangler samtykke-scenario og har svart nei pa BOSTOTTE`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = false)
        assertSkalVise(true)
    }

    @Test
    fun `Mangler samtykke-scenario og har ikke svart BOSTOTTE`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BEKREFTELSE_BOUTGIFTER, verdi = true)
        assertSkalVise(true)
    }

    @Test
    fun `Har samtykke, men ingen bostottesaker eller -utbetalinger`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)

        assertSkalVise(true)
    }

    private fun `Finnes bostottesaker scenario`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)
        okonomiService.addBostotteSaker(soknad.id, createBostotteSak("Beskrivelse av sak"))

        assertSkalVise(false)
    }

    @Test
    fun `Finnes bostotte-utbetalinger scenario`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)
        okonomiService.addElementToOkonomi(soknad.id, InntektType.UTBETALING_HUSBANKEN)

        assertSkalVise(false)
    }

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(opprettSoknad())
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
