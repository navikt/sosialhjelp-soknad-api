package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.MobiltelefonService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.KontaktRegisterService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TelefonnummerHandler(
    private val mobiltelefonService: MobiltelefonService,
    private val kontaktService: KontaktRegisterService,
) : RegisterDataFetcher {
    override fun fetchAndSave(soknadId: UUID) {
        mobiltelefonService.hent(getUserIdFromToken())
            ?.let { norskTelefonnummer(it) }
            ?.also { kontaktService.updateTelefonRegister(soknadId, it) }
    }

    private fun norskTelefonnummer(mobiltelefonnummer: String?): String? {
        return mobiltelefonnummer?.let { tlf ->
            if (tlf.length == 8) {
                "+47$tlf"
            } else {
                tlf.takeIf { it.startsWith("+47") && tlf.length == 11 }
            }
        }
    }
}
