package no.nav.sosialhjelp.soknad.oppsummering.steg.kort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.BostotteHusbanken
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.NavUtbetalinger
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.SaldoBrukskonto
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.SkattbarInntekt

object SituasjonsendringSteg {
    private val bostotteHusbanken = BostotteHusbanken()
    private val skatt = SkattbarInntekt()
    private val navUtbetalinger = NavUtbetalinger()
    private val saldoBrukskonto = SaldoBrukskonto()

    fun get(json: JsonInternalSoknad): Steg {
        val soknad = requireNotNull(json.soknad)
        val okonomi = requireNotNull(soknad.data.okonomi)
        return Steg(
            stegNr = 4,
            tittel = "situasjon.kort.tittel",
            avsnitt =
                listOf(
                    skatt.getAvsnitt(okonomi, soknad.driftsinformasjon),
                    bostotteHusbanken.getAvsnitt(okonomi.opplysninger, soknad.driftsinformasjon, autoConfirmation = true),
                    navUtbetalinger.getAvsnitt(okonomi.opplysninger, soknad.driftsinformasjon),
                    saldoBrukskonto.getAvsnitt(requireNotNull(okonomi.oversikt)),
                ),
        )
    }
}
