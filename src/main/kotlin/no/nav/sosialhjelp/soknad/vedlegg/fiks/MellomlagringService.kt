package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils.isNonProduction
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.finnVedleggEllerKastException
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FilKonvertering
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.VedleggWrapper
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.*

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val virusScanner: VirusScanner,
    private val soknadUnderArbeidService: SoknadUnderArbeidService
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
        sourceData: ByteArray,
        originalfilnavn: String,
    ): Pair<String, MellomlagretVedleggMetadata> {

        val vedleggWrapper = FilKonvertering.konverterHvisStottet(sourceData, originalfilnavn)
        val filOpplasting = opprettFilOpplasting(vedleggWrapper, behandlingsId)

        val navEksternId = getNavEksternId(behandlingsId)
        mellomlagringClient.postVedlegg(navEksternId = navEksternId, filOpplasting = filOpplasting)

        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        val filId = mellomlagredeVedlegg?.firstOrNull { it.filnavn == filOpplasting.metadata.filnavn }?.filId
            ?: throw IllegalStateException("Klarte ikke finne det mellomlagrede vedlegget som akkurat ble lastet opp")

        return Pair(
            getSha512FromByteArray(vedleggWrapper.data),
            MellomlagretVedleggMetadata(
                filnavn = filOpplasting.metadata.filnavn,
                filId = filId
            )
        )
    }

    private fun opprettFilOpplasting(vedleggWrapper: VedleggWrapper, behandlingsId: String): FilOpplasting {
        return with(vedleggWrapper) {

            val fileType = validerFil(data, filnavn)
            virusScanner.scan(filnavn, data, behandlingsId, fileType.name)

            FilOpplasting(
                metadata = FilMetadata(
                    filnavn = lagFilnavn(filnavn, fileType, UUID.nameUUIDFromBytes(data).toString()),
                    mimetype = detectMimeType(data),
                    storrelse = data.size.toLong()
                ),
                data = ByteArrayInputStream(data)
            )
        }
    }

    fun oppdaterSoknadUnderArbeid(
        sha512: String?,
        behandlingsId: String,
        vedleggstype: String,
        filnavn: String,
    ) {
        if (sha512 == null) throw IllegalStateException()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        if (jsonVedlegg.filer == null) {
            jsonVedlegg.filer = ArrayList()
        }
        jsonVedlegg.withStatus(Vedleggstatus.LastetOpp.toString()).filer.add(
            JsonFiler().withFilnavn(filnavn).withSha512(sha512)
        )
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
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

        // oppdater soknadUnderArbeid
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val jsonVedlegg: JsonVedlegg = JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
            .firstOrNull {
                it.filer.any { jsonFil -> jsonFil.filnavn == aktueltVedlegg.filnavn }
            }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører en utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")

        jsonVedlegg.filer.removeIf { it.filnavn == aktueltVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
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
            soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(soknadUnderArbeid)
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
