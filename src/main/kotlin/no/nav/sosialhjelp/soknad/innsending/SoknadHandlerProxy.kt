package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.HttpServletResponse
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.dto.BekreftelseRessurs
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleController
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteService
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektSkatteetatenService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadHandlerProxy(
    private val soknadMetadataService: SoknadMetadataService,
    private val jsonInternalSoknadGenerator: JsonInternalSoknadGenerator,
    private val registerDataService: RegisterDataService,
    private val lifecycleController: SoknadLifecycleController,
    private val bostotteService: BostotteService,
    private val inntektSkatteetatenService: InntektSkatteetatenService,
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

    fun isKort(soknadId: UUID) =
        soknadMetadataService.getSoknadType(soknadId) ==
            SoknadType.KORT
                .also { logger.info("Soknad $soknadId er kort: $it") }

    private fun createJson(soknadId: String) =
        jsonInternalSoknadGenerator.createJsonInternalSoknad(UUID.fromString(soknadId))

    fun cancelSoknad(
        soknadId: String,
        referer: String?,
    ) {
        lifecycleController.deleteSoknad(UUID.fromString(soknadId), referer)
    }

    fun updateSamtykker(
        soknadId: String,
        hasBostotteSamtykke: Boolean,
        hasSkatteetatenSamtykke: Boolean,
        token: String?,
    ) {
        bostotteService.updateSamtykke(UUID.fromString(soknadId), hasBostotteSamtykke, token)
        inntektSkatteetatenService.updateSamtykkeSkatt(UUID.fromString(soknadId), hasSkatteetatenSamtykke)
    }

    fun getSamtykker(
        behandlingsId: String,
        token: String?,
    ): List<BekreftelseRessurs> {
        return listOfNotNull(
            bostotteService.getBostotteInfo(UUID.fromString(behandlingsId)).samtykke?.verdi?.let { samtykke ->
                BekreftelseRessurs(
                    type = BekreftelseType.BOSTOTTE_SAMTYKKE.toJsonTypeSamtykke(),
                    verdi = samtykke,
                )
            },
            inntektSkatteetatenService.getSamtykkeSkatt(UUID.fromString(behandlingsId))?.verdi?.let { samtykke ->
                BekreftelseRessurs(
                    type = BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE.toJsonTypeSamtykke(),
                    verdi = samtykke,
                )
            },
        )
    }

    companion object {
        private val logger by logger()
    }
}

private fun BekreftelseType.toJsonTypeSamtykke() =
    when (this) {
        BekreftelseType.BOSTOTTE_SAMTYKKE -> SoknadJsonTyper.BOSTOTTE_SAMTYKKE
        BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE -> SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
        else -> throw IllegalArgumentException("Invalid BekreftelseType (samtykke): $this")
    }
