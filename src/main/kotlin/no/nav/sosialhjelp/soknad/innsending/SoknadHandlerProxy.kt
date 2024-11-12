package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleController
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadHandlerProxy(
    private val soknadMetadataService: SoknadMetadataService,
    private val jsonInternalSoknadGenerator: JsonInternalSoknadGenerator,
    private val registerDataService: RegisterDataService,
    private val lifecycleController: SoknadLifecycleController,
) {
    fun updateLastChanged(soknadId: String) {
        soknadMetadataService.updateLastChanged(UUID.fromString(soknadId))
    }

    // TODO Se på en bedre løsning for dette "spørsmålet" - flytte ansvaret til hver registerdata-fetcher??
    fun isRegisterdataChanged(soknadId: String): Boolean {
        // lag json fra eksisterende data
        return createJson(soknadId)
            // kjør ny innhenting av registerdata
            .also { registerDataService.runAllRegisterDataFetchers(UUID.fromString(soknadId)) }
            // sammenlikne eksisterende etter ny innhenting
            .let { existing -> existing != createJson(soknadId) }
    }

    fun createSoknad(
        soknadstype: String?,
        response: HttpServletResponse,
    ): StartSoknadResponse {
        return lifecycleController.createSoknad(soknadstype, response)
            .let { StartSoknadResponse(it.soknadId.toString(), it.useKortSoknad) }
            .also { logger.info("Starter soknad med ny datamodell") }
    }

    fun getSoknadType(soknadId: UUID) = soknadMetadataService.getSoknadType(soknadId) == SoknadType.KORT

    private fun createJson(soknadId: String) =
        jsonInternalSoknadGenerator.createJsonInternalSoknad(UUID.fromString(soknadId))

    fun cancelSoknad(
        soknadId: String,
        referer: String?,
    ) {
        lifecycleController.deleteSoknad(UUID.fromString(soknadId), referer)
    }

    companion object {
        private val logger by logger()
    }
}
