package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils.isNonProduction
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val virusScanner: VirusScanner
) {

    fun getAllVedlegg(behandlingsId: String): List<MellomlagretVedleggMetadata> {
        val navEksternId = getNavEksternId(behandlingsId)
        return mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)
            ?.mellomlagringMetadataList
            ?.map {
                MellomlagretVedleggMetadata(
                    filnavn = it.filnavn,
                    filId = it.filId
                )
            } ?: emptyList()
    }

    fun getVedlegg(behandlingsId: String, vedleggId: String): MellomlagretVedlegg? {
        val navEksternId = getNavEksternId(behandlingsId)
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.warn("Ingen mellomlagrede vedlegg funnet ved forsøkt henting av vedleggId $vedleggId")
        }
        return mellomlagredeVedlegg
            ?.firstOrNull { it.filId == vedleggId }
            ?.filnavn
            ?.let {
                MellomlagretVedlegg(
                    filnavn = it,
                    data = mellomlagringClient.getVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId)
                )
            }
    }

    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        orginalData: ByteArray,
        orginaltFilnavn: String
    ): MellomlagretVedleggMetadata {
        virusScanner.scan(orginaltFilnavn, orginalData, behandlingsId, detectMimeType(orginalData))

        val (filnavn, data) = VedleggUtils.behandleFilOgReturnerFildata(orginaltFilnavn, orginalData)
        soknadUnderArbeidService.sjekkDuplikate(behandlingsId, filnavn)

        val filOpplasting = opprettFilOpplasting(filnavn, data)

        val navEksternId = getNavEksternId(behandlingsId)
        mellomlagringClient.postVedlegg(navEksternId = navEksternId, filOpplasting = filOpplasting)

        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        val filId = mellomlagredeVedlegg?.firstOrNull { it.filnavn == filOpplasting.metadata.filnavn }?.filId
            ?: throw IllegalStateException("Klarte ikke finne det mellomlagrede vedlegget som akkurat ble lastet opp")

        return MellomlagretVedleggMetadata(
            filnavn = filOpplasting.metadata.filnavn,
            filId = filId
        )
    }

    private fun opprettFilOpplasting(filnavn: String, data: ByteArray): FilOpplasting {
        return FilOpplasting(
            data = ByteArrayInputStream(data),
            metadata = FilMetadata(
                filnavn = filnavn,
                mimetype = detectMimeType(data),
                storrelse = data.size.toLong()
            )
        )
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String, vedleggId: String) {
        val navEksternId = getNavEksternId(behandlingsId)

        // hent alle mellomlagrede vedlegg
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.warn("Ingen mellomlagrede vedlegg funnet ved forsøkt sletting av vedleggId $vedleggId")
            return
        }

        val aktueltVedlegg = mellomlagredeVedlegg.firstOrNull { it.filId == vedleggId } ?: return
        soknadUnderArbeidService.fjernVedleggFraInternalSoknad(behandlingsId, aktueltVedlegg)

        mellomlagringClient.deleteVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId)
    }

    fun deleteVedlegg(behandlingsId: String, vedleggId: String) {
        val navEksternId = getNavEksternId(behandlingsId)
        mellomlagringClient.deleteVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId)
    }

    fun deleteAllVedlegg(behandlingsId: String) {
        val navEksternId = getNavEksternId(behandlingsId)
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg.isNullOrEmpty()) {
            log.info("Ingen mellomlagrede vedlegg funnet ved forsøkt sletting av alle vedlegg for behandlingsId $behandlingsId")
        } else {
            mellomlagringClient.deleteAllVedlegg(navEksternId = navEksternId)
        }
    }

    private fun getNavEksternId(behandlingsId: String) =
        if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId

    fun kanSoknadHaMellomlagredeVedleggForSletting(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        val kanSoknadSendesMedDigisosApi = try {
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

data class MellomlagretVedleggMetadata(
    val filnavn: String,
    val filId: String,
    val sha512: String? = null
)

data class MellomlagretVedlegg(
    val filnavn: String,
    val data: ByteArray
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
