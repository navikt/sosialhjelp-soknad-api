package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi

object JsonOkonomiUtils {
    fun isOkonomiskeOpplysningerBekreftet(jsonOkonomi: JsonOkonomi): Boolean {
        return jsonOkonomi.opplysninger.bekreftelse != null && jsonOkonomi.opplysninger.bekreftelse.isNotEmpty()
    }
}
