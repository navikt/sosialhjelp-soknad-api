package no.nav.sosialhjelp.soknad.repository.okonomi

import no.nav.sosialhjelp.soknad.domene.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.domene.okonomi.InntektRepository
import no.nav.sosialhjelp.soknad.domene.okonomi.Komponent
import no.nav.sosialhjelp.soknad.domene.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.domene.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*

class InntektRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var inntektRepository: InntektRepository

    @Test
    fun `Lagre inntekt`() {
        val soknad = opprettSoknad()
        val soknad2 = opprettSoknad()

        createFullInntekt(soknadId = soknad.id).also { inntektRepository.save(it) }
        createFullInntekt(soknadId = soknad.id).also { inntektRepository.save(it) }
        createFullInntekt(soknadId = soknad2.id).also { inntektRepository.save(it) }

        val findAll = inntektRepository.findAll()
        findAll.size
    }
}


fun createFullInntekt(soknadId: UUID): Inntekt {
    return Inntekt (
        soknadId = soknadId,
        type = OkonomiType.BARNEBIDRAG,
        tittel = "Tittel",
        brutto = 235,
        netto = 128,
        utbetaling = Utbetaling (
            orgnummer = "123152151",
            belop = 1412,
            skattetrekk = 241.44,
            andreTrekk = 21512.22,
            utbetalingsdato = LocalDate.now(),
            periodeStart = LocalDate.of(2022, 12, 1),
            periodeSlutt = LocalDate.of(2022, 12, 31),
            setOf(
                Komponent(
                    type = "Type",
                    belop = 1231.22,
                    satsType = "Satstype",
                    satsBelop = 1251.22
                )
            )
        ),
        bekreftelse = Bekreftelse(
            soknadId = soknadId,
            type = "Type bekreftelse",
            tittel = "Tittel bekreftelse",
            bekreftet = true
        )
    )
}