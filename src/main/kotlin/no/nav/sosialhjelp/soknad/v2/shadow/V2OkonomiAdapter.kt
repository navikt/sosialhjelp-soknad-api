package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.inntekt.andreinntekter.UtbetalingRessurs
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend

interface V2OkonomiAdapter {
    fun updateOkonomiskeOpplysninger(
        behandlingsId: String,
        vedleggFrontend: VedleggFrontend,
    )

    fun updateUtbetalinger(
        behandlingsId: String,
        utbetalingerFrontend: UtbetalingRessurs.UtbetalingerFrontend,
    )
}
