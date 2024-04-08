package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.springframework.stereotype.Component

@Component
class TelefonnummerSystemdata(
    private val mobiltelefonService: MobiltelefonService,
    private val v2AdapterService: V2AdapterService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return

        val personalia = jsonInternalSoknad.soknad.data.personalia
        val telefonnummer = personalia.telefonnummer
        if (telefonnummer == null || telefonnummer.kilde == JsonKilde.SYSTEM) {
            val personIdentifikator = personalia.personIdentifikator.verdi
            val systemverdi = innhentSystemverdiTelefonnummer(personIdentifikator)
            personalia.telefonnummer = getTelefonnummer(systemverdi, telefonnummer)

            // NyModell
            v2AdapterService.updateTelefonRegister(soknadUnderArbeid.behandlingsId, systemverdi)
        }
    }

    fun innhentSystemverdiTelefonnummer(personIdentifikator: String): String? {
        return try {
            norskTelefonnummer(mobiltelefonService.hent(personIdentifikator))
        } catch (e: Exception) {
            log.warn("Kunne ikke hente telefonnummer fra Krr", e)
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
        return mobiltelefonnummer.takeIf { it.startsWith("+47") && it.length == 11 }
    }

    companion object {
        private val log by logger()

        private fun getTelefonnummer(systemverdi: String?, telefonnummer: JsonTelefonnummer?): JsonTelefonnummer? {
            return when {
                systemverdi == null -> null
                telefonnummer != null -> telefonnummer.withVerdi(systemverdi)
                else -> JsonTelefonnummer().withKilde(JsonKilde.SYSTEM).withVerdi(systemverdi)
            }
        }
    }
}
