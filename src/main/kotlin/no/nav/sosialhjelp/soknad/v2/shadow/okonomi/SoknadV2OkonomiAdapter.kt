package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.v2.shadow.V2OkonomiAdapter
import org.springframework.stereotype.Component

@Component
class SoknadV2OkonomiAdapter() : V2OkonomiAdapter {
    override fun updateOkonomiskeOpplysninger(
        behandlingsId: String,
        vedleggFrontend: VedleggFrontend,
    ) {
        // TODO Oppdatere rader med OkonomiskeDetaljer på OkonomiElement

        // TODO Oppdatere status på vedlegg
    }
}
