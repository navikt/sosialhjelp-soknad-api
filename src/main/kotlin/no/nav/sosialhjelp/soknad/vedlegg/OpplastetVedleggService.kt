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

        return VedleggUtils.validateAndReturnNewFilename(orginaltFilnavn, data)
            .let { nyttFilnavn ->

                val documentId = mellomlagringService.uploadVedlegg(behandlingsId, dokumentasjonType, data, nyttFilnavn)

                addVedleggToSoknad(data.sha512(), behandlingsId, dokumentasjonType, nyttFilnavn, documentId)

                // nyModell
                dokumentasjonAdapter.saveDokumentMetadata(
                    behandlingsId,
                    dokumentasjonType,
                    documentId,
                    nyttFilnavn,
                    data.sha512(),
                )
                Pair(nyttFilnavn, documentId)
            }
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

    fun deleteAllFromMellomlagring(behandlingsId: String) {
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

    private fun addVedleggToSoknad(
        sha512: String,
        behandlingsId: String,
        dokumentasjonType: String,
        nyttFilnavn: String,
        documentId: String,
    ) {
        runCatching {
            soknadUnderArbeidService.addVedleggToSoknad(
                sha512 = sha512,
                behandlingsId = behandlingsId,
                vedleggstype = dokumentasjonType,
                filnavn = nyttFilnavn,
            )
        }.onFailure {
            logger.error("Kunne ikke legge til vedlegg i søknad under arbeid", it)
            mellomlagringService.deleteVedlegg(behandlingsId, documentId)
        }
    }

    companion object {
        private val logger by logger()
    }
}

private fun ByteArray.sha512() = VedleggUtils.getSha512FromByteArray(this)
