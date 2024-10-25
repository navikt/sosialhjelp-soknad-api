package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.v2.shadow.DokumentasjonAdapter
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Service

@Service
class OpplastetVedleggService(
    private val mellomlagringService: MellomlagringService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val virusScanner: VirusScanner,
    private val dokumentasjonAdapter: DokumentasjonAdapter,
) {
    fun uploadDocument(
        behandlingsId: String,
        dokumentasjonType: String,
        orginaltFilnavn: String,
        data: ByteArray,
    ): Pair<String, String> {
        virusScanner.scan(orginaltFilnavn, data, behandlingsId, detectMimeType(data))

        val nyttFilnavn = VedleggUtils.validateAndReturnNewFilename(orginaltFilnavn, data)

        val documentId =
            mellomlagringService.uploadVedlegg(
                behandlingsId = behandlingsId,
                vedleggstype = dokumentasjonType,
                data = data,
                filnavn = nyttFilnavn,
            )

        runCatching {
            soknadUnderArbeidService.addVedleggToSoknad(
                sha512 = VedleggUtils.getSha512FromByteArray(data),
                behandlingsId = behandlingsId,
                vedleggstype = dokumentasjonType,
                filnavn = nyttFilnavn,
                filId = documentId,
            )
        }.onFailure {
            logger.error("Kunne ikke legge til vedlegg i søknad under arbeid", it)
            mellomlagringService.deleteVedlegg(behandlingsId, documentId)
        }

        // nyModell
        dokumentasjonAdapter.saveDokumentMetadata(
            behandlingsId = behandlingsId,
            vedleggTypeString = dokumentasjonType,
            dokumentId = documentId,
            filnavn = nyttFilnavn,
            sha512 = VedleggUtils.getSha512FromByteArray(data),
        )

        return Pair(nyttFilnavn, documentId)
    }

    fun deleteDocument(
        behandlingsId: String,
        documentId: String,
    ) {
        mellomlagringService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, documentId)
            ?.also { soknadUnderArbeidService.fjernVedleggFraInternalSoknad(behandlingsId, it) }

        // nyModell
        dokumentasjonAdapter.deleteDokumentMetadata(
            behandlingsId = behandlingsId,
            dokumentId = documentId,
        )
    }

    fun deleteAllVedlegg(behandlingsId: String) {
        logger.info("Sletter alle dokumenter hos FIKS for behandlingsId: $behandlingsId")
        mellomlagringService.deleteAllVedlegg(behandlingsId)
    }

    fun kanSoknadHaMellomlagredeVedleggForSletting(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        val kanSoknadSendesMedDigisosApi =
            try {
                soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(soknadUnderArbeid.behandlingsId)
            } catch (e: Exception) {
                false
            }
        return kanSoknadSendesMedDigisosApi
    }

    companion object {
        private val logger by logger()
    }
}
