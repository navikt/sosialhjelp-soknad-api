package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.finnVedleggEllerKastException
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OpplastetVedleggService(
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val virusScanner: VirusScanner
) {
    fun saveVedleggAndUpdateVedleggstatus(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        originalfilnavn: String
    ): OpplastetVedlegg {
        var filnavn = originalfilnavn

        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val sha512 = getSha512FromByteArray(data)

        val fileType = validerFil(data, filnavn)
        virusScanner.scan(filnavn, data, behandlingsId, fileType.name)

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val soknadId = soknadUnderArbeid.soknadId

        val uuid = UUID.randomUUID().toString()
        filnavn = lagFilnavn(filnavn, fileType, uuid)

        val opplastetVedlegg = OpplastetVedlegg(
            uuid = uuid,
            eier = eier,
            vedleggType = OpplastetVedleggType(vedleggstype),
            data = data,
            soknadId = soknadId,
            filnavn = filnavn,
            sha512 = sha512
        )
        opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier)

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        if (jsonVedlegg.filer == null) {
            jsonVedlegg.filer = ArrayList()
        }
        jsonVedlegg.withStatus(Vedleggstatus.LastetOpp.toString()).filer.add(
            JsonFiler().withFilnavn(filnavn).withSha512(sha512)
        )

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        return opplastetVedlegg
    }

    fun sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId: String?, data: ByteArray) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val soknadId = soknadUnderArbeid.soknadId

        val samletVedleggStorrelse = opplastetVedleggRepository.hentSamletVedleggStorrelse(soknadId, eier)
        val newStorrelse = samletVedleggStorrelse + data.size
        if (newStorrelse > MAKS_SAMLET_VEDLEGG_STORRELSE) {
            val feilmeldingId = if (soknadUnderArbeid.erEttersendelse) {
                "ettersending.vedlegg.feil.samletStorrelseForStor"
            } else {
                "vedlegg.opplasting.feil.samletStorrelseForStor"
            }
            throw SamletVedleggStorrelseForStorException(
                "Kunne ikke lagre fil fordi samlet størrelse på alle vedlegg er for stor",
                null,
                feilmeldingId
            )
        }
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String?, vedleggId: String?) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier) ?: return

        val vedleggstype = opplastetVedlegg.vedleggType.sammensattType

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        jsonVedlegg.filer.removeIf { it.sha512 == opplastetVedlegg.sha512 && it.filnavn == opplastetVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
        opplastetVedleggRepository.slettVedlegg(vedleggId, eier)
    }

    companion object {
        const val MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB = 150
        const val MAKS_SAMLET_VEDLEGG_STORRELSE = MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB * 1024 * 1024 // 150 MB
    }
}
