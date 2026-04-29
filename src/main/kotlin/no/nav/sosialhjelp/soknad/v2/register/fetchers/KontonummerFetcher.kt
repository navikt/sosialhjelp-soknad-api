package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService
import no.nav.sosialhjelp.soknad.v2.eier.service.EierRegisterService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KontonummerFetcher(
    private val kontonummerService: KontonummerService,
    private val eierService: EierRegisterService,
) : RegisterDataFetcher {
    override fun fetchAndSave(soknadId: UUID) {
        kontonummerService.getKontonummer(SubjectHandlerUtils.getUserIdFromToken())
            ?.let { eierService.updateKontonummerFromRegister(soknadId, kontonummerRegister = it) }
    }
}
