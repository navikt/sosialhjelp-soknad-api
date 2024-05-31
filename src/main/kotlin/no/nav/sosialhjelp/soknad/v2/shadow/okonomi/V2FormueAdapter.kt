package no.nav.sosialhjelp.soknad.v2.shadow.okonomi

import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs

interface V2FormueAdapter {
    fun leggTilFormue(
        behandlingsId: String,
        formueFrontend: FormueRessurs.FormueFrontend,
    )
}
