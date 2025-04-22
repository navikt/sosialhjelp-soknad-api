package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleController
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadHandlerProxy(
    private val lifecycleController: SoknadLifecycleController,
) {
    fun createSoknad(
        soknadstype: String?,
        response: HttpServletResponse,
    ): StartSoknadResponse {
        return lifecycleController.createSoknad(soknadstype, response)
            .let { StartSoknadResponse(it.soknadId.toString(), it.useKortSoknad) }
    }

    fun isKort(soknadId: UUID) = lifecycleController.isKortSoknad(soknadId.toString())

    fun cancelSoknad(
        soknadId: String,
        referer: String?,
    ) {
        lifecycleController.deleteSoknad(UUID.fromString(soknadId), referer)
    }
}
