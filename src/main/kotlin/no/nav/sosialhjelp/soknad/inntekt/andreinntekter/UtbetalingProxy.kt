package no.nav.sosialhjelp.soknad.inntekt.andreinntekter

import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.HarIkkeUtbetalingerInput
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.HarUtbetalingerInput
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.UtbetalingController
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UtbetalingProxy(private val utbetalingController: UtbetalingController) {
    fun getUtbetalinger(behandlingsId: String): UtbetalingRessurs.UtbetalingerFrontend {
        return utbetalingController
            .getUtbetalinger(UUID.fromString(behandlingsId))
            .let {
                UtbetalingRessurs.UtbetalingerFrontend(
                    bekreftelse = it.hasBekreftelse,
                    utbytte = it.hasUtbytte,
                    salg = it.hasSalg,
                    forsikring = it.hasForsikring,
                    annet = it.hasAnnenUtbetaling,
                    beskrivelseAvAnnet = it.beskrivelseUtbetaling,
                )
            }
    }

    fun updateUtbetalinger(
        behandlingsId: String,
        utbetalingerFrontend: UtbetalingRessurs.UtbetalingerFrontend,
    ) {
        with(utbetalingerFrontend) {
            bekreftelse
                ?.let { harBekreftelse ->

                    if (harBekreftelse) {
                        HarUtbetalingerInput(
                            hasUtbytte = utbytte,
                            hasSalg = salg,
                            hasForsikring = forsikring,
                            hasAnnet = annet,
                            beskrivelseUtbetaling = beskrivelseAvAnnet,
                        )
                    } else {
                        HarIkkeUtbetalingerInput()
                    }
                }
                ?.let { input ->
                    utbetalingController.updateUtbetalinger(
                        soknadId = UUID.fromString(behandlingsId),
                        input = input,
                    )
                }
        }
    }
}
