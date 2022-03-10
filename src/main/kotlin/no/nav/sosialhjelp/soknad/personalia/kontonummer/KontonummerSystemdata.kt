package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.common.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid

class KontonummerSystemdata(
    private val kontonummerService: KontonummerService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val personalia = soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.personalia ?: return

        val kontonummer = personalia.kontonummer
        val personIdentifikator = personalia.personIdentifikator.verdi
        if (kontonummer.kilde == JsonKilde.SYSTEM) {
            val systemverdi = innhentSystemverdiKontonummer(personIdentifikator)
            if (systemverdi == null || systemverdi.isEmpty()) {
                kontonummer.kilde = JsonKilde.BRUKER
                kontonummer.verdi = null
            } else {
                val verdi = systemverdi.replace("\\D".toRegex(), "")
                kontonummer.verdi = verdi
            }
        }
    }

    fun innhentSystemverdiKontonummer(personIdentifikator: String): String? {
        return kontonummerService.getKontonummer(personIdentifikator)
    }
}
