package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2FormueAdapter(
    private val okonomiRepository: OkonomiRepository,
) : V2FormueAdapter {
    override fun leggTilFormue(
        behandlingsId: String,
        formueFrontend: FormueRessurs.FormueFrontend,
    ) {
    }

    private fun leggTilBekreftelse() {
    }

    private fun leggTilBeskrivelseAvAnnet() {
    }
}
