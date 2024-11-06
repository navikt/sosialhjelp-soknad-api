package no.nav.sosialhjelp.soknad.inntekt.verdi

import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarIkkeVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdiController
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdierDto
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdierInput
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class VerdiProxy(private val verdiController: VerdiController) {
    fun getVerdier(behandlingsId: String): VerdiRessurs.VerdierFrontend {
        return verdiController.getVerdier(UUID.fromString(behandlingsId)).toVerdierFrontend()
    }

    fun updateVerdier(
        behandlingsId: String,
        verdierFrontend: VerdiRessurs.VerdierFrontend,
    ) {
        verdierFrontend.toVerdierInput()
            ?.let { input ->
                verdiController.updateVerdier(
                    soknadId = UUID.fromString(behandlingsId),
                    input = input,
                )
            }
    }
}

private fun VerdierDto.toVerdierFrontend(): VerdiRessurs.VerdierFrontend {
    return VerdiRessurs.VerdierFrontend(
        bekreftelse = bekreftelse,
        bolig = hasBolig,
        campingvogn = hasCampingvogn,
        kjoretoy = hasKjoretoy,
        fritidseiendom = hasFritidseiendom,
        annet = hasAnnetVerdi,
        beskrivelseAvAnnet = beskrivelseVerdi,
    )
}

private fun VerdiRessurs.VerdierFrontend.toVerdierInput(): VerdierInput? {
    // Hvis bekreftelse ikke er satt, er det ikke nødvendig å gjøre noe
    return bekreftelse?.let {
        if (!bekreftelse) {
            HarIkkeVerdierInput()
        } else {
            HarVerdierInput(
                hasBekreftelse = true,
                hasBolig = bolig,
                hasCampingvogn = campingvogn,
                hasKjoretoy = kjoretoy,
                hasFritidseiendom = fritidseiendom,
                hasBeskrivelseVerdi = annet,
                beskrivelseVerdi = beskrivelseAvAnnet,
            )
        }
    }
}
