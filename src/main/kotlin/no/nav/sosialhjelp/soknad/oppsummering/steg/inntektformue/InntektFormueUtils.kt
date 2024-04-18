package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

object InntektFormueUtils {
    fun harBekreftelse(
        opplysninger: JsonOkonomiopplysninger,
        type: String,
    ): Boolean {
        return opplysninger.bekreftelse != null && opplysninger.bekreftelse.any { type == it.type }
    }

    fun harBekreftelseTrue(
        opplysninger: JsonOkonomiopplysninger,
        type: String,
    ): Boolean {
        return opplysninger.bekreftelse.any { type == it.type && java.lang.Boolean.TRUE == it.verdi }
    }

    fun getBekreftelse(
        opplysninger: JsonOkonomiopplysninger,
        type: String,
    ): JsonOkonomibekreftelse? {
        return opplysninger.bekreftelse.firstOrNull { type == it.type }
    }

    fun harValgtFormueType(
        oversikt: JsonOkonomioversikt?,
        type: String,
    ): Boolean {
        return oversikt?.formue?.any { type == it.type } ?: false
    }

    fun addFormueIfPresent(
        oversikt: JsonOkonomioversikt?,
        felter: MutableList<Felt>,
        type: String,
        key: String,
    ) {
        oversikt?.formue
            ?.firstOrNull { type == it.type }
            ?.let {
                felter.add(
                    Felt(
                        type = Type.CHECKBOX,
                        svar = createSvar(key, SvarType.LOCALE_TEKST),
                    ),
                )
            }
    }
}
