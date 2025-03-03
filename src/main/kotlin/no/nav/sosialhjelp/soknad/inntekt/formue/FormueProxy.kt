package no.nav.sosialhjelp.soknad.inntekt.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueController
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueDto
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueInput
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadV2FormueAdapter.Companion.logger
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FormueProxy(private val formueController: FormueController) {
    fun getFormue(behandlingsId: String): FormueRessurs.FormueFrontend {
        return formueController.getFormue(UUID.fromString(behandlingsId)).toFormueFrontend()
    }

    fun updateFormue(
        behandlingsId: String,
        formueFrontend: FormueRessurs.FormueFrontend,
    ) {
        logger.info("Oppdaterer formuer.")

        formueController.updateFormue(
            soknadId = UUID.fromString(behandlingsId),
            input = formueFrontend.toFormueInput(),
        )
    }
}

private fun FormueDto.toFormueFrontend() =
    FormueRessurs.FormueFrontend(
        brukskonto = hasBrukskonto,
        sparekonto = hasSparekonto,
        bsu = hasBsu,
        livsforsikring = hasLivsforsikring,
        verdipapirer = hasVerdipapirer,
        annet = hasSparing,
        beskrivelseAvAnnet = beskrivelseSparing,
    )

private fun FormueRessurs.FormueFrontend.toFormueInput() =
    FormueInput(
        hasBrukskonto = brukskonto,
        hasSparekonto = sparekonto,
        hasBsu = bsu,
        hasLivsforsikring = livsforsikring,
        hasVerdipapirer = verdipapirer,
        hasBeskrivelseSparing = annet,
        beskrivelseSparing = beskrivelseAvAnnet,
    )
