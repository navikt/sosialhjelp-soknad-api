package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import org.slf4j.LoggerFactory

class TelefonnummerSystemdata(
    private val mobiltelefonService: MobiltelefonService
) : Systemdata {
    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid, token: String?) {
        val personalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        val telefonnummer = personalia.telefonnummer
        if (telefonnummer == null || telefonnummer.kilde == JsonKilde.SYSTEM) {
            val personIdentifikator = personalia.personIdentifikator.verdi
            val systemverdi = innhentSystemverdiTelefonnummer(personIdentifikator)
            personalia.telefonnummer =
                if (systemverdi == null) null else if (telefonnummer != null) telefonnummer.withVerdi(systemverdi) else JsonTelefonnummer()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(systemverdi)
        }
    }

    fun innhentSystemverdiTelefonnummer(personIdentifikator: String?): String? {
        return try {
            norskTelefonnummer(mobiltelefonService.hent(personIdentifikator!!))
        } catch (e: Exception) {
            log.warn("Kunne ikke hente telefonnummer fra Dkif", e)
            null
        }
    }

    private fun norskTelefonnummer(mobiltelefonnummer: String?): String? {
        if (mobiltelefonnummer == null) {
            return null
        }
        if (mobiltelefonnummer.length == 8) {
            return "+47$mobiltelefonnummer"
        }
        return if (mobiltelefonnummer.startsWith("+47") && mobiltelefonnummer.length == 11) {
            mobiltelefonnummer
        } else null
    }

    companion object {
        private val log = LoggerFactory.getLogger(TelefonnummerSystemdata::class.java)
    }
}
