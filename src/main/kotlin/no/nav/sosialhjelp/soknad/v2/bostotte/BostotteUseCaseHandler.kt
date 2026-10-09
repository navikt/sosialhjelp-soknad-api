package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BostotteUseCaseHandler(
    private val bostotteService: BostotteService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) {
    fun getBostotteInfo(soknadId: UUID): Pair<BostotteInfo, Boolean?> {
        return Pair(
            bostotteService.getBostotteInfo(soknadId),
            integrasjonStatusService.hasFetchHusbankenFailed(soknadId),
        )
    }

}
