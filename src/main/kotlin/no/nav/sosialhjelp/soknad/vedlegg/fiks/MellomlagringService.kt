package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils.isNonProduction
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.finnVedleggEllerKastException
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes.APPLICATION_PDF
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes.TEXT_X_MATLAB
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.ws.rs.NotFoundException

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val virusScanner: VirusScanner,
    private val unleash: Unleash,
    private val kommuneInfoService: KommuneInfoService,
    @Value("\${mellomlagring_start}") private val mellomlagringStartString: String
) {

    fun getAllVedlegg(behandlingsId: String): List<MellomlagretVedleggMetadata> {
        val navEksternId = if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId
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
        val navEksternId = if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg == null) {
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
        data: ByteArray,
        originalfilnavn: String,
    ): MellomlagretVedleggMetadata {
        var filnavn = originalfilnavn

        val fileType = validerFil(data, filnavn)
        virusScanner.scan(filnavn, data, behandlingsId, fileType.name)

        val uuid = UUID.randomUUID().toString()
        filnavn = lagFilnavn(filnavn, fileType, uuid)

        val detectedMimeType = FileDetectionUtils.getMimeType(data)
        val mimetype = if (detectedMimeType.equals(TEXT_X_MATLAB, ignoreCase = true)) APPLICATION_PDF else detectedMimeType

        val filOpplasting = FilOpplasting(
            metadata = FilMetadata(
                filnavn = filnavn,
                mimetype = mimetype,
                storrelse = data.size.toLong()
            ),
            data = ByteArrayInputStream(data)
        )

        val navEksternId = if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId

        mellomlagringClient.postVedlegg(navEksternId = navEksternId, filOpplasting = filOpplasting)

        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList

        val filId = mellomlagredeVedlegg?.firstOrNull { it.filnavn == filnavn }?.filId
            ?: throw IllegalStateException("Klarte ikke finne det mellomlagrede vedlegget som akkurat ble lastet opp")

        // oppdater SoknadUnderArbeid etter suksessfull opplasting
        oppdaterSoknadUnderArbeid(data, behandlingsId, vedleggstype, filnavn)

        return MellomlagretVedleggMetadata(filnavn = filnavn, filId = filId)
    }

    private fun oppdaterSoknadUnderArbeid(
        data: ByteArray,
        behandlingsId: String,
        vedleggstype: String,
        filnavn: String,
    ) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val sha512 = getSha512FromByteArray(data)

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
        val navEksternId = if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId

        // hent alle mellomlagrede vedlegg
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = navEksternId)?.mellomlagringMetadataList
        if (mellomlagredeVedlegg == null) {
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
            } ?: throw NotFoundException("Dette vedlegget tilhører en utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")

        jsonVedlegg.filer.removeIf { it.filnavn == aktueltVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
        mellomlagringClient.deleteVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId)
    }

    fun deleteVedlegg(behandlingsId: String, vedleggId: String) {
        val navEksternId = if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId
        mellomlagringClient.deleteVedlegg(navEksternId = navEksternId, digisosDokumentId = vedleggId)
    }

    fun deleteAllVedlegg(behandlingsId: String) {
        if (getAllVedlegg(behandlingsId).isNotEmpty()) {
            val navEksternId = if (isNonProduction()) createPrefixedBehandlingsId(behandlingsId) else behandlingsId
            mellomlagringClient.deleteAllVedlegg(navEksternId = navEksternId)
        }
    }

    fun erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        return unleash.isEnabled(KS_MELLOMLAGRING_ENABLED, false) &&
            soknadSkalSendesMedDigisosApi(soknadUnderArbeid) &&
            soknadOpprettetEtterAktiveringAvMellomlagring(soknadUnderArbeid)
    }

    fun kanSoknadHaMellomlagredeVedleggForSletting(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        val kanSoknadSendesMedDigisosApi = try {
            soknadSkalSendesMedDigisosApi(soknadUnderArbeid)
        } catch (e: Exception) {
            false
        }

        return unleash.isEnabled(KS_MELLOMLAGRING_ENABLED, false) &&
            kanSoknadSendesMedDigisosApi &&
            soknadOpprettetEtterAktiveringAvMellomlagring(soknadUnderArbeid)
    }

    private fun soknadSkalSendesMedDigisosApi(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        if (soknadUnderArbeid.erEttersendelse) {
            return false
        }

        val kommunenummer = soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            ?: return false.also { log.info("Mottaker.kommunenummer ikke satt -> soknadSkalSendesMedDigisosApi returnerer false") }

        return when (kommuneInfoService.kommuneInfo(kommunenummer)) {
            KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException("Mellomlagring av vedlegg er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.")
            }
            KommuneStatus.MANGLER_KONFIGURASJON, KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> false
            KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> true
            KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException("Sending til kommune $kommunenummer er midlertidig utilgjengelig.")
            }
        }
    }

    private fun soknadOpprettetEtterAktiveringAvMellomlagring(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        if (mellomlagringStartString.isEmpty()) return false
        val mellomlagringStart = LocalDateTime.parse(mellomlagringStartString, dateTimeFormatter)
        return soknadUnderArbeid.opprettetDato.isEqual(mellomlagringStart) || soknadUnderArbeid.opprettetDato.isAfter(mellomlagringStart)
    }

    companion object {
        private val log by logger()
        private const val format = "dd.MM.yyyy HH:mm:ss"
        private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(format)

        private const val KS_MELLOMLAGRING_ENABLED = "sosialhjelp.soknad.ks-mellomlagring-enabled"
    }
}

data class MellomlagretVedleggMetadata(
    val filnavn: String,
    val filId: String
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
