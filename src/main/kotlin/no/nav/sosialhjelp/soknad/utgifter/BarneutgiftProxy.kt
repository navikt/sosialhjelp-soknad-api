package no.nav.sosialhjelp.soknad.utgifter

import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BarneutgiftController
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BarneutgifterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarBarneutgifterInput
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarIkkeBarneutgifterInput
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BarneutgiftProxy(private val barneutgiftController: BarneutgiftController) {
    fun getBarneutgifter(soknadId: String): BarneutgiftRessurs.BarneutgifterFrontend {
        return barneutgiftController
            .getBarneutgifter(UUID.fromString(soknadId))
            .toBarneutgifterFrontend()
    }

    fun updateBarneutgifter(
        behandlingsId: String,
        barneutgifterFrontend: BarneutgiftRessurs.BarneutgifterFrontend,
    ) {
        if (!barneutgifterFrontend.harForsorgerplikt) return
        if (barneutgifterFrontend.bekreftelse == null) return

        val input =
            barneutgifterFrontend.let {
                if (barneutgifterFrontend.bekreftelse) {
                    HarBarneutgifterInput(
                        hasFritidsaktiviteter = it.fritidsaktiviteter,
                        hasSfo = it.sfo,
                        hasTannregulering = it.tannregulering,
                        hasBarnehage = it.barnehage,
                        hasAnnenUtgiftBarn = it.annet,
                    )
                } else {
                    HarIkkeBarneutgifterInput()
                }
            }
        barneutgiftController.updateBarneutgifter(
            soknadId = UUID.fromString(behandlingsId),
            input = input,
        )
    }
}

private fun BarneutgifterDto.toBarneutgifterFrontend(): BarneutgiftRessurs.BarneutgifterFrontend {
    return BarneutgiftRessurs.BarneutgifterFrontend(
        bekreftelse = hasBekreftelse,
        harForsorgerplikt = hasForsorgerplikt,
        fritidsaktiviteter = hasFritidsaktiviteter,
        barnehage = hasBarnehage,
        sfo = hasSfo,
        tannregulering = hasTannregulering,
        annet = hasAnnenUtgiftBarn,
    )
}
