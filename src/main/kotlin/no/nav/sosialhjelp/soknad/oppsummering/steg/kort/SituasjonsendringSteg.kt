package no.nav.sosialhjelp.soknad.oppsummering.steg.kort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.BostotteHusbanken
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.NavUtbetalinger
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.SkattbarInntekt

class SituasjonsendringSteg {
    private val bostotteHusbanken = BostotteHusbanken()
    private val skatt = SkattbarInntekt()
    private val navUtbetalinger = NavUtbetalinger()

    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val situasjonsendring = jsonInternalSoknad.soknad.data.situasjonendring

        return Steg(
            stegNr = 4,
            tittel = "situasjon.kort.tittel",
            avsnitt =
                listOf(
                    skatt.getAvsnitt(jsonInternalSoknad.soknad.data.okonomi, jsonInternalSoknad.soknad.driftsinformasjon),
                    bostotteHusbanken.getAvsnitt(jsonInternalSoknad.soknad.data.okonomi.opplysninger, jsonInternalSoknad.soknad.driftsinformasjon, autoConfirmation = true),
                    navUtbetalinger.getAvsnitt(jsonInternalSoknad.soknad.data.okonomi.opplysninger, jsonInternalSoknad.soknad.driftsinformasjon),
                ),
        )
    }

    private fun Boolean?.toFelt() =
        this?.let {
            listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar =
                        createSvar(
                            it.toLocaleTekst(),
                            SvarType.LOCALE_TEKST,
                        ),
                ),
            )
        }

    private fun String?.toFelt() =
        this?.let {
            listOf(
                Felt(
                    type = Type.TEKST,
                    svar =
                        createSvar(
                            it,
                            SvarType.TEKST,
                        ),
                ),
            )
        }

    private fun Boolean.toLocaleTekst() = if (this) "avbryt.ja" else "avbryt.nei"
}
