package no.nav.sosialhjelp.soknad.oppsummering.steg.kort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.BostotteHusbanken
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.NavUtbetalinger
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.SaldoBrukskonto
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.SkattbarInntekt

class SituasjonsendringSteg {
    private val bostotteHusbanken = BostotteHusbanken()
    private val skatt = SkattbarInntekt()
    private val navUtbetalinger = NavUtbetalinger()
    private val saldoBrukskonto = SaldoBrukskonto()

    fun get(json: JsonInternalSoknad): Steg {
        return Steg(
            stegNr = 4,
            tittel = "situasjon.kort.tittel",
            avsnitt =
                listOf(
                    skatt.getAvsnitt(json.soknad.data.okonomi, json.soknad.driftsinformasjon),
                    bostotteHusbanken.getAvsnitt(json.soknad.data.okonomi.opplysninger, json.soknad.driftsinformasjon, autoConfirmation = true),
                    navUtbetalinger.getAvsnitt(json.soknad.data.okonomi.opplysninger, json.soknad.driftsinformasjon),
                    saldoBrukskonto.getAvsnitt(json.soknad.data.okonomi.oversikt),
                ),
        )
    }
}
