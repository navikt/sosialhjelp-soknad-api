package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.StudielanController
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.StudielanInput
import no.nav.sosialhjelp.soknad.v2.shadow.runWithNestedTransaction
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

interface V2InntektAdapter {
    fun leggTilStudielan(
        behandlingsId: String,
        inputDto: StudielanRessurs.StudielanInputDTO,
    )
}

@Service
class SoknadV2InntektAdapter(
    private val studielanController: StudielanController,
    private val transactionTemplate: TransactionTemplate,
) : V2InntektAdapter {
    override fun leggTilStudielan(
        behandlingsId: String,
        inputDto: StudielanRessurs.StudielanInputDTO,
    ) {
        inputDto.bekreftelse?.let { hasStudielan ->
            transactionTemplate.runWithNestedTransaction {
                studielanController.updateStudielan(
                    soknadId = UUID.fromString(behandlingsId),
                    input = StudielanInput(mottarStudielan = hasStudielan),
                )
            }
        }
    }
}
