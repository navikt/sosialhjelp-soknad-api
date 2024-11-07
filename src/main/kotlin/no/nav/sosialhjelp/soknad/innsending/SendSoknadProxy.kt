package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.innsending.dto.SendTilUrlFrontend
import no.nav.sosialhjelp.soknad.innsending.dto.SoknadMottakerFrontend
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleController
import no.nav.sosialhjelp.soknad.v2.SoknadSendtDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SendSoknadProxy(
    private val lifecycleController: SoknadLifecycleController,
    private val dokumentasjonService: DokumentasjonService,
    private val soknadMetadataService: SoknadMetadataService,
) {
    fun sendSoknad(soknadId: String): SendTilUrlFrontend {
        return lifecycleController
            .sendSoknad(UUID.fromString(soknadId))
            .toSendTilUrlFrontend(
                forrigeSoknadSendt = getForrigeSoknadSendt(soknadId),
                antallDokumenter = getNumberOfDocuments(soknadId),
            )
    }

    private fun getForrigeSoknadSendt(soknadId: String): LocalDateTime? {
        return soknadMetadataService.getAllSoknaderMetadataForBruker(UUID.fromString(soknadId))
            ?.let { metadataList ->
                metadataList
                    .filter { it.soknadId != UUID.fromString(soknadId) }
                    .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }
                    .ifEmpty { null }
                    ?.map { metadata -> metadata.tidspunkt.sendtInn ?: error("SoknadMetadata skal ha tidspunkt for sendt inn") }
                    ?.maxByOrNull { it }
            }
    }

    private fun getNumberOfDocuments(soknadId: String): Int {
        return dokumentasjonService.findDokumentasjonForSoknad(UUID.fromString(soknadId))
            .flatMap { it.dokumenter }
            .count()
    }
}

private fun SoknadSendtDto.toSendTilUrlFrontend(
    forrigeSoknadSendt: LocalDateTime?,
    antallDokumenter: Int,
) =
    SendTilUrlFrontend(
        id = digisosId.toString(),
        sendtTil = SoknadMottakerFrontend.FIKS_DIGISOS_API,
        forrigeSoknadSendt = forrigeSoknadSendt,
        antallDokumenter = antallDokumenter,
    )
