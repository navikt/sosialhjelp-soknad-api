package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService
import no.nav.sosialhjelp.soknad.v2.eier.service.EierRegisterService
import no.nav.sosialhjelp.soknad.v2.register.SynchronousFetcher
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KontonummerFetcher(
    private val kontonummerService: KontonummerService,
    private val eierService: EierRegisterService,
) : SynchronousFetcher {
    override suspend fun fetchAndSave(
        soknadId: UUID,
    ) {
        kontonummerService.getKontonummer()
            ?.let { eierService.updateKontonummerFromRegister(soknadId, kontonummerRegister = it) }
    }
}
