package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils.isNonProduction
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.UUID

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
) {
    fun getAllVedlegg(soknadId: UUID): List<MellomlagretVedleggMetadata> = getAllVedlegg(soknadId.toString())

    fun getAllVedlegg(behandlingsId: String): List<MellomlagretVedleggMetadata> {
        val navEksternId = getNavEksternId(behandlingsId)
        return mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)
            ?.mellomlagringMetadataList
            ?.map {
                MellomlagretVedleggMetadata(
                    filnavn = it.filnavn,
                    filId = it.filId,
                )
            } ?: emptyList()
    }

    fun getVedlegg(
        behandlingsId: String,
        vedleggId: String,
    ): MellomlagretVedlegg? {
        val navEksternId = getNavEksternId(behandlingsId)
        val mellomlagredeVedlegg =
            mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.warn("Ingen mellomlagrede vedlegg funnet ved forsøkt henting av vedleggId $vedleggId")
        }
        return mellomlagredeVedlegg
            ?.firstOrNull { it.filId == vedleggId }
            ?.filnavn
            ?.let {
                MellomlagretVedlegg(
                    filnavn = it,
                    data = mellomlagringClient.getVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId),
                )
            }
    }

    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        filnavn: String,
    ): String {
        return mellomlagringClient
            .postVedlegg(
                navEksternId = getNavEksternId(behandlingsId),
                filOpplasting = opprettFilOpplasting(filnavn, data),
            )
            .mellomlagringMetadataList
            ?.let { it[0].filId }
            ?.also { filId -> log.info("Fil med filId $filId er lastet opp") }
            ?: throw FiksException("MellomlarginDto er null", null)
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

    fun deleteVedleggAndUpdateVedleggstatus(
        behandlingsId: String,
        documentId: String,
    ): MellomlagringDokumentInfo? {
        val navEksternId = getNavEksternId(behandlingsId)

        val mellomlagredeVedlegg =
            mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList

        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.warn("Ingen mellomlagrede vedlegg funnet ved forsøkt sletting av vedleggId $documentId")
            return null
        }

        return mellomlagredeVedlegg.firstOrNull { it.filId == documentId }
            ?.also {
                mellomlagringClient.deleteVedlegg(navEksternId = navEksternId, digisosDokumentId = documentId)
            }
    }

    fun deleteVedlegg(
        behandlingsId: String,
        vedleggId: String,
    ) {
        val navEksternId = getNavEksternId(behandlingsId)
        mellomlagringClient.deleteVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId)
    }

    fun deleteAllVedlegg(behandlingsId: String) {
        val navEksternId = getNavEksternId(behandlingsId)
        val mellomlagredeVedlegg =
            mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.info("Ingen mellomlagrede vedlegg funnet ved forsøkt sletting av alle vedlegg for behandlingsId $behandlingsId")
        } else {
            mellomlagringClient.deleteAllVedlegg(navEksternId = navEksternId)
        }
    }

    fun deleteAll(soknadId: UUID) {
        mellomlagringClient.deleteAllVedlegg(soknadId.toString())
    }

    // TODO Kan formålet gjøres annerledes (miljøvariable etc.) for å unngå miljøspesifikk logikk i koden
    private fun getNavEksternId(behandlingsId: String) =
        if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId

    companion object {
        private val log by logger()
    }
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
