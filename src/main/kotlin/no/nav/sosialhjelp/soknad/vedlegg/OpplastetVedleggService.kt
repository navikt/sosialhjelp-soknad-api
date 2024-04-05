package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.finnVedleggEllerKastException
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Component
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@Component
class OpplastetVedleggService(
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val virusScanner: VirusScanner
) {
    fun lastOppVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        orginalData: ByteArray,
        orginaltFilnavn: String
    ): OpplastetVedlegg {
        virusScanner.scan(orginaltFilnavn, orginalData, behandlingsId, FileDetectionUtils.detectMimeType(orginalData))

        val (filnavn, data) = VedleggUtils.behandleFilOgReturnerFildata(orginaltFilnavn, orginalData)
        // TODO - denne sjekken er egentlig bortkastet sålenge filnavnet genereres av randomUUID()
        soknadUnderArbeidService.sjekkDuplikate(behandlingsId, filnavn)

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        return OpplastetVedlegg(
            eier = eier(),
            vedleggType = OpplastetVedleggType(vedleggstype),
            data = data,
            soknadId = soknadUnderArbeid.soknadId,
            filnavn = filnavn
        ).also {
            opplastetVedleggRepository.opprettVedlegg(it, eier())
            soknadUnderArbeidService.oppdaterSoknadUnderArbeid(
                getSha512FromByteArray(data),
                behandlingsId,
                vedleggstype,
                filnavn
            )
        }
    }

    fun sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId: String?, data: ByteArray) {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val soknadId = soknadUnderArbeid.soknadId

        val samletVedleggStorrelse = opplastetVedleggRepository.hentSamletVedleggStorrelse(soknadId, eier())
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
        val opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier()) ?: return

        val vedleggstype = opplastetVedlegg.vedleggType.sammensattType

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        jsonVedlegg.filer.removeIf { it.sha512 == opplastetVedlegg.sha512 && it.filnavn == opplastetVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier())
        opplastetVedleggRepository.slettVedlegg(vedleggId, eier())
    }

    companion object {
        const val MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB = 150
        const val MAKS_SAMLET_VEDLEGG_STORRELSE = MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB * 1024 * 1024 // 150 MB
    }
}
