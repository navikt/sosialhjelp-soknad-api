package no.nav.sosialhjelp.soknad.inntekt.studielan

import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.StudielanController
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.StudielanInput
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class StudielanProxy(private val studielanController: StudielanController) {
    fun getStudielan(soknadId: String): StudielanRessurs.StudielanFrontend {
        return studielanController
            .getHasStudielan(UUID.fromString(soknadId))
            .let {
                StudielanRessurs.StudielanFrontend(
                    skalVises = it.erStudent ?: false,
                    bekreftelse = it.mottarStudielan,
                )
            }
    }

    fun leggTilStudielan(
        behandlingsId: String,
        inputDto: StudielanRessurs.StudielanInputDTO,
    ): StudielanRessurs.StudielanFrontend {
        inputDto.bekreftelse?.let { hasStudielan ->
            studielanController.updateStudielan(
                soknadId = UUID.fromString(behandlingsId),
                input = StudielanInput(mottarStudielan = hasStudielan),
            )
        }
        return getStudielan(behandlingsId)
    }
}
