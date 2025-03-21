package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType

class SaldoBrukskonto {
    fun getAvsnitt(
        oversikt: JsonOkonomioversikt,
    ): Avsnitt {
        return Avsnitt(
            tittel = "utbetalinger.inntekt.skattbar.oppsummering_kort_saldo_tittel",
            sporsmal = saldoBrukskontoSporsmal(oversikt),
        )
    }

    private fun saldoBrukskontoSporsmal(
        oversikt: JsonOkonomioversikt,
    ): List<Sporsmal> {
        return oversikt.formue
            .filter { it.type == jsonTypeVerdi }
            .let { formuer ->
                if (formuer.all { it.belop == null }) {
                    createEmptySporsmal()
                } else {
                    createSporsmalForSaldo(formuer)
                }
            }
    }

    private fun createSporsmalForSaldo(formuer: List<JsonOkonomioversiktFormue>): List<Sporsmal> {
        return formuer.map {
            Sporsmal(
                tittel = "utbetalinger.inntekt.skattbar.oppsummering_kort_saldo_sporsmal",
                erUtfylt = true,
                felt =
                    listOf(
                        Felt(
                            type = Type.TEKST,
                            svar = createSvar(it.belop.toString() + " kr", SvarType.TEKST),
                        ),
                    ),
            )
        }
    }

    private fun createEmptySporsmal(): List<Sporsmal> {
        return listOf(
            Sporsmal(
                tittel = "utbetalinger.inntekt.skattbar.oppsummering_kort_konto_saldo_ingen",
                erUtfylt = false,
                felt = null,
            ),
        )
    }

    companion object {
        private val jsonTypeVerdi: String =
            OpplysningTypeMapper
                .getJsonVerdier(FormueType.FORMUE_BRUKSKONTO).navn?.verdi
                ?: error("Finnes ikke mapping for type")
    }
}
