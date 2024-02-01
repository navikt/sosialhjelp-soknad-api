package no.nav.sosialhjelp.soknad.metrics

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadataListe
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.getProsent

object VedleggskravStatistikkUtil {

    private val log by logger()
    private val mapper = jacksonObjectMapper()

    fun genererVedleggskravStatistikk(json: JsonInternalSoknad): VedleggskravStatistikk {

        val vedleggList = convertToVedleggMetadataListe(json).vedleggListe

        var antallInnsendt = 0
        var antallLevertTidligere = 0
        var antallIkkeLevert = 0
        var totaltAntall = 0
        for (vedlegg in vedleggList) {
            if (!JsonVedleggUtils.isVedleggskravAnnet(vedlegg)) {
                totaltAntall++
                when (vedlegg.status) {
                    Vedleggstatus.LastetOpp -> antallInnsendt++
                    Vedleggstatus.VedleggAlleredeSendt -> antallLevertTidligere++
                    Vedleggstatus.VedleggKreves -> antallIkkeLevert++
                    else -> continue
                }
            }
        }

        return VedleggskravStatistikk(
            sendetype = "soknad",
            totaltAntall = totaltAntall,
            antallInnsendt = antallInnsendt,
            antallLevertTidligere = antallLevertTidligere,
            antallIkkeLevert = antallIkkeLevert,
            prosentInnsendt = getProsent(antallInnsendt, totaltAntall),
            prosentLevertTidligere = getProsent(antallLevertTidligere, totaltAntall),
            prosentIkkeLevert = getProsent(antallIkkeLevert, totaltAntall)
        ).also {
            log.info("Vedleggskrav statistikk: ${mapper.writeValueAsString(it)}")
        }
    }

    @Deprecated("Bruker SoknadUnderArbeid - utgår med ny datamodell")
    fun genererOgLoggVedleggskravStatistikk(
        soknadUnderArbeid: SoknadUnderArbeid,
        vedleggList: List<VedleggMetadata>
    ) {
        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeid, vedleggList)
        log.info("Vedleggskrav statistikk: ${mapper.writeValueAsString(vedleggStatistikk)}")
    }

    @Deprecated("Bruker SoknadUnderArbeid - utgår med ny datamodell")
    fun genererVedleggskravStatistikk(
        soknadUnderArbeid: SoknadUnderArbeid,
        vedleggList: List<VedleggMetadata>
    ): VedleggskravStatistikk {
        val isEttersendelse = soknadUnderArbeid.erEttersendelse

        var antallInnsendt = 0
        var antallLevertTidligere = 0
        var antallIkkeLevert = 0
        var totaltAntall = 0
        for (vedlegg in vedleggList) {
            if (!JsonVedleggUtils.isVedleggskravAnnet(vedlegg)) {
                totaltAntall++
                when (vedlegg.status) {
                    Vedleggstatus.LastetOpp -> antallInnsendt++
                    Vedleggstatus.VedleggAlleredeSendt -> antallLevertTidligere++
                    Vedleggstatus.VedleggKreves -> antallIkkeLevert++
                    else -> continue
                }
            }
        }

        return VedleggskravStatistikk(
            sendetype = if (isEttersendelse) "ettersendelse" else "soknad",
            totaltAntall = totaltAntall,
            antallInnsendt = antallInnsendt,
            antallLevertTidligere = antallLevertTidligere,
            antallIkkeLevert = antallIkkeLevert,
            prosentInnsendt = getProsent(antallInnsendt, totaltAntall),
            prosentLevertTidligere = getProsent(antallLevertTidligere, totaltAntall),
            prosentIkkeLevert = getProsent(antallIkkeLevert, totaltAntall)
        )
    }

    private fun convertToVedleggMetadataListe(json: JsonInternalSoknad): VedleggMetadataListe {
        val vedleggMetadataListe = VedleggMetadataListe()

        vedleggMetadataListe.vedleggListe = json.vedlegg.vedlegg
            .map {
                VedleggMetadata(
                    skjema = it.type,
                    tillegg = it.tilleggsinfo,
                    filnavn = it.type,
                    status = Vedleggstatus.valueOf(it.status),
                )
            }.toMutableList()
        return vedleggMetadataListe
    }

    data class VedleggskravStatistikk(
        val sendetype: String,
        val totaltAntall: Int,
        val antallInnsendt: Int,
        val antallLevertTidligere: Int,
        val antallIkkeLevert: Int,
        val prosentInnsendt: Int,
        val prosentLevertTidligere: Int,
        val prosentIkkeLevert: Int
    )
}
