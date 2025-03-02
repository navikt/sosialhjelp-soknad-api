package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgiftController
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgifterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarBoutgifterInput
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarIkkeBoutgifterInput
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BoutgifterProxy(private val boutgiftController: BoutgiftController) {
    fun getBoutgifter(behandlingsId: String): BoutgifterFrontend {
        return boutgiftController
            .getBoutgifter(UUID.fromString(behandlingsId))
            .toBoutgifterFrontend()
    }

    fun updateBoutgifter(
        behandlingsId: String,
        boutgifterFrontend: BoutgifterFrontend,
    ) {
        if (boutgifterFrontend.bekreftelse == null) return

        val input =
            if (boutgifterFrontend.bekreftelse) {
                HarBoutgifterInput(
                    hasHusleie = boutgifterFrontend.husleie,
                    hasStrom = boutgifterFrontend.strom,
                    hasOppvarming = boutgifterFrontend.oppvarming,
                    hasKommunalAvgift = boutgifterFrontend.kommunalAvgift,
                    hasBoliglan = boutgifterFrontend.boliglan,
                    hasAnnenBoutgift = boutgifterFrontend.annet,
                )
            } else {
                HarIkkeBoutgifterInput()
            }

        boutgiftController.updateBoutgifter(
            soknadId = UUID.fromString(behandlingsId),
            input = input,
        )
    }
}

private fun BoutgifterDto.toBoutgifterFrontend() =
    BoutgifterFrontend(
        bekreftelse = bekreftelse,
        husleie = husleie,
        strom = strom,
        kommunalAvgift = kommunalAvgift,
        oppvarming = oppvarming,
        boliglan = boliglan,
        annet = annet,
        skalViseInfoVedBekreftelse = skalViseInfoVedBekreftelse,
    )
