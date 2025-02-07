package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.util.UUID

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val virusScanner: VirusScanner,
) {
    @Deprecated("Bruk DokumentlagerService")
    fun getAllVedlegg(behandlingsId: String): List<MellomlagretVedleggMetadata> {
        // todo enhetlig løsning - eller fjerne helt
//        val navEksternId = getNavEksternId(behandlingsId)
        return mellomlagringClient.hentDokumenterMetadata(navEksternId = behandlingsId)
            ?.mellomlagringMetadataList
            ?.map {
                MellomlagretVedleggMetadata(
                    filnavn = it.filnavn,
                    filId = it.filId,
                )
            } ?: emptyList()
    }

    @Deprecated("Bruk DokumentlagerService")
    fun getVedlegg(
        behandlingsId: String,
        vedleggId: String,
    ): MellomlagretVedlegg? {
        // todo enhetlig løsning - eller fjerne helt
//        val navEksternId = getNavEksternId(behandlingsId)
        val mellomlagredeVedlegg =
            mellomlagringClient.hentDokumenterMetadata(navEksternId = behandlingsId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.warn("Ingen mellomlagrede vedlegg funnet ved forsøkt henting av vedleggId $vedleggId")
        }
        return mellomlagredeVedlegg
            ?.firstOrNull { it.filId == vedleggId }
            ?.filnavn
            ?.let {
                MellomlagretVedlegg(
                    filnavn = it,
                    data = mellomlagringClient.hentDokument(navEksternId = behandlingsId, digisosDokumentId = vedleggId),
                )
            }
    }

    @Deprecated("Bruk DokumentlagerService")
    @Transactional
    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        orginalData: ByteArray,
        orginaltFilnavn: String,
    ): MellomlagretVedleggMetadata {
        virusScanner.scan(orginaltFilnavn, orginalData, behandlingsId, detectMimeType(orginalData))

        val (filnavn, data) = VedleggUtils.validerFilOgReturnerNyttFilnavnOgData(orginaltFilnavn, orginalData)
        // TODO - denne sjekken er egentlig bortkastet sålenge filnavnet genereres av randomUUID()
        soknadUnderArbeidService.sjekkDuplikate(behandlingsId, filnavn)

        val sha512 = VedleggUtils.getSha512FromByteArray(data)
        soknadUnderArbeidService.oppdaterSoknadUnderArbeid(
            sha512,
            behandlingsId,
            vedleggstype,
            filnavn,
        )

        val filOpplasting = opprettFilOpplasting(filnavn, data)
        // todo enhetlig løsning - eller fjerne helt
//        val navEksternId = getNavEksternId(behandlingsId)

        val filId =
            mellomlagringClient.lastOppDokument(navEksternId = behandlingsId, filOpplasting = filOpplasting)
                .getFirstDocumentIdOrThrow()

        return MellomlagretVedleggMetadata(
            filnavn = filOpplasting.metadata.filnavn,
            filId = filId,
        )
            .also { log.info("Fil med filId $filId er lastet opp") }
    }

    private fun opprettFilOpplasting(
        filnavn: String,
        data: ByteArray,
    ): FilOpplasting {
        return FilOpplasting(
            data = ByteArrayInputStream(data),
            metadata =
                FilMetadata(
                    filnavn = filnavn,
                    mimetype = detectMimeType(data),
                    storrelse = data.size.toLong(),
                ),
        )
    }

    @Deprecated("Bruk DokumentlagerService")
    fun deleteVedlegg(
        behandlingsId: String,
        vedleggId: String,
    ) {
        // todo enhetlig løsning - eller fjerne helt

//        val navEksternId = getNavEksternId(behandlingsId)
        mellomlagringClient.slettDokument(navEksternId = behandlingsId, digisosDokumentId = vedleggId)
    }

    @Deprecated("Bruk DokumentlagerService")
    fun deleteAllVedlegg(behandlingsId: String) {
        // todo enhetlig løsning - eller fjerne helt
//        val navEksternId = getNavEksternId(behandlingsId)
        val mellomlagredeVedlegg =
            mellomlagringClient.hentDokumenterMetadata(navEksternId = behandlingsId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.info("Ingen mellomlagrede vedlegg funnet ved forsøkt sletting av alle vedlegg for behandlingsId $behandlingsId")
        } else {
            mellomlagringClient.slettAlleDokumenter(navEksternId = behandlingsId)
        }
    }

    @Deprecated("Bruk DokumentlagerService")
    fun deleteAll(soknadId: UUID) {
        mellomlagringClient.slettAlleDokumenter(soknadId.toString())
    }

    // TODO Kan formålet gjøres annerledes (miljøvariable etc.) for å unngå miljøspesifikk logikk i koden
    // todo lag en enhetlig løsning - eller fjern helt
//    private fun getNavEksternId(behandlingsId: String) =
//        if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId

    @Deprecated("Bruk DokumentlagerService")
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
        private val log by logger()
    }
}

private fun MellomlagringDto.getFirstDocumentIdOrThrow(): String {
    return mellomlagringMetadataList
        ?.firstOrNull()
        ?.filId
        ?: throw IllegalStateException("Klarte ikke finne det mellomlagrede vedlegget som akkurat ble lastet opp")
}

data class MellomlagretVedleggMetadata(
    val filnavn: String,
    val filId: String,
    val sha512: String? = null,
)

data class MellomlagretVedlegg(
    val filnavn: String,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MellomlagretVedlegg

        if (filnavn != other.filnavn) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filnavn.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
