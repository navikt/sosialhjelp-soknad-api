package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.v2.okonomi.AbstractOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BelopDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BoliglanInput
import no.nav.sosialhjelp.soknad.v2.okonomi.GenericOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInntektDto
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeOpplysningerController
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.shadow.V2OkonomiAdapter
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Component
class SoknadV2OkonomiAdapter(
    private val okonomiskeOpplysningerController: OkonomiskeOpplysningerController,
    private val transactionTemplate: TransactionTemplate,
) : V2OkonomiAdapter {
    override fun updateOkonomiskeOpplysninger(
        behandlingsId: String,
        vedleggFrontend: VedleggFrontend,
    ) {
        runWithNestedTransaction {
            okonomiskeOpplysningerController.updateOkonomiskeDetaljer(
                soknadId = UUID.fromString(behandlingsId),
                input = vedleggFrontend.resolveOkonomiInput(),
            )
        }
            .onFailure { logger.warn("NyModell: Feil i oppdatering av Okonomiske Opplysninger", it) }
    }

    private fun runWithNestedTransaction(function: () -> Unit): Result<Unit> {
        return kotlin.runCatching {
            transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_NESTED
            transactionTemplate.execute { function.invoke() }
        }
    }

    companion object {
        private val logger by logger()
    }
}

private fun VedleggFrontend.resolveOkonomiInput(): AbstractOkonomiInput {
    val opplysningType: OpplysningType =
        type.opplysningType
            ?: throw IllegalArgumentException("VedleggType ${type.name} har ingen mapping til OpplysningType")

    return when (opplysningType) {
        InntektType.JOBB -> toLonnsInput()
        UtgiftType.UTGIFTER_BOLIGLAN -> toBoliglanInput()
        else -> toGenericOkonomiInput(opplysningType)
    }
}

private fun VedleggFrontend.toLonnsInput() =
    LonnsInput(
        dokumentasjonLevert = alleredeLevert ?: false,
        detalj = rader?.first().let { LonnsInntektDto(brutto = it?.brutto?.toDouble(), netto = it?.netto?.toDouble()) },
    )

private fun VedleggFrontend.toBoliglanInput() =
    BoliglanInput(
        dokumentasjonLevert = alleredeLevert ?: false,
        detaljer = rader?.map { AvdragRenterDto(it.avdrag?.toDouble(), it.renter?.toDouble()) } ?: emptyList(),
    )

private fun VedleggFrontend.toGenericOkonomiInput(opplysningType: OpplysningType) =
    GenericOkonomiInput(
        opplysningType = opplysningType,
        dokumentasjonLevert = alleredeLevert ?: false,
        detaljer =
            rader?.map {
                BelopDto(beskrivelse = it.beskrivelse, belop = it.belop?.toDouble() ?: 0.0)
            } ?: emptyList(),
    )
