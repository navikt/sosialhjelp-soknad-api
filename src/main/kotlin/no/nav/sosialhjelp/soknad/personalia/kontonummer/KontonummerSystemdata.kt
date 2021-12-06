package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid

class KontonummerSystemdata(
    private val kontonummerService: KontonummerService
) : Systemdata {
    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid, token: String) {
        val personalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
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

    fun innhentSystemverdiKontonummer(personIdentifikator: String?): String? {
        return kontonummerService.getKontonummer(personIdentifikator!!)
    }
}
