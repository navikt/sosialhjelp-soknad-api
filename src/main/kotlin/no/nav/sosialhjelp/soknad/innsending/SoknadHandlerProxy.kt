package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleController
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadHandlerProxy(
    private val soknadMetadataService: SoknadMetadataService,
    private val lifecycleController: SoknadLifecycleController,
) {
    fun createSoknad(
        soknadstype: String?,
        response: HttpServletResponse,
    ): StartSoknadResponse {
        return lifecycleController.createSoknad(soknadstype, response)
            .let { StartSoknadResponse(it.soknadId.toString(), it.useKortSoknad) }
    }

    fun isKort(soknadId: UUID) = soknadMetadataService.getSoknadType(soknadId) == SoknadType.KORT

    fun cancelSoknad(
        soknadId: String,
        referer: String?,
    ) {
        lifecycleController.deleteSoknad(UUID.fromString(soknadId), referer)
    }
}
