package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs
import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueController
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarIkkeVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdiController
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdierInput
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

interface V2FormueAdapter {
    fun leggTilFormue(
        behandlingsId: String,
        formueFrontend: FormueRessurs.FormueFrontend,
    )

    fun leggTilVerdi(
        behandlingsId: String,
        verdierFrontend: VerdiRessurs.VerdierFrontend,
    )
}

interface V2FormueAdapter {
    fun leggTilFormue(
        behandlingsId: String,
        formueFrontend: FormueRessurs.FormueFrontend,
    )
}

@Service
class SoknadV2FormueAdapter(
    private val formueController: FormueController,
    private val verdiController: VerdiController,
    private val transactionTemplate: TransactionTemplate,
) : V2FormueAdapter {
    override fun leggTilFormue(
        behandlingsId: String,
        formueFrontend: FormueRessurs.FormueFrontend,
    ) {
        logger.info("NyModell: Oppdaterer formuer.")

        runWithNestedTransaction {
            formueController.updateFormue(
                soknadId = UUID.fromString(behandlingsId),
                input = formueFrontend.toFormueInput(),
            )
        }
            .onFailure { logger.warn("NyModell: Legge til ny formue feilet", it) }
    }

    override fun leggTilVerdi(
        behandlingsId: String,
        verdierFrontend: VerdiRessurs.VerdierFrontend,
    ) {
        logger.info("NyModell: Oppdaterer verdier.")

        verdierFrontend.toVerdierInput()?.let { input ->
            runWithNestedTransaction {
                verdiController.updateVerdier(
                    soknadId = UUID.fromString(behandlingsId),
                    input = input,
                )
            }
        }
    }

    private fun runWithNestedTransaction(function: () -> Unit): Result<Unit> {
        return kotlin.runCatching {
            transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_NESTED
            transactionTemplate.execute { function.invoke() }
        }
    }

    companion object {
        val logger by logger()
    }
}

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
