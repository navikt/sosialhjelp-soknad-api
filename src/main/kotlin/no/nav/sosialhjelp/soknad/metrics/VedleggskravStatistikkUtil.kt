package no.nav.sosialhjelp.soknad.metrics

import jakarta.xml.bind.annotation.XmlRootElement
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.getProsent
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil.ANNET
import tools.jackson.module.kotlin.jacksonObjectMapper

object VedleggskravStatistikkUtil {
    private val log by logger()
    private val mapper = jacksonObjectMapper()
    const val ANNET = "annet"

    fun genererVedleggskravStatistikk(json: JsonInternalSoknad): VedleggskravStatistikk {
        val vedleggList = convertToVedleggMetadataListe(json).vedleggListe

        var antallInnsendt = 0
        var antallLevertTidligere = 0
        var antallIkkeLevert = 0
        var totaltAntall = 0
        for (vedlegg in vedleggList) {
            if (!isVedleggskravAnnet(vedlegg)) {
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
            prosentIkkeLevert = getProsent(antallIkkeLevert, totaltAntall),
        ).also {
            log.info("Vedleggskrav statistikk: ${mapper.writeValueAsString(it)}")
        }
    }

    fun genererVedleggskravStatistikk(
        vedleggList: List<VedleggMetadata>,
    ): VedleggskravStatistikk {
        var antallInnsendt = 0
        var antallLevertTidligere = 0
        var antallIkkeLevert = 0
        var totaltAntall = 0
        for (vedlegg in vedleggList) {
            if (!isVedleggskravAnnet(vedlegg)) {
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
            prosentIkkeLevert = getProsent(antallIkkeLevert, totaltAntall),
        )
    }

    private fun convertToVedleggMetadataListe(json: JsonInternalSoknad): VedleggMetadataListe {
        val vedleggMetadataListe = VedleggMetadataListe()

        vedleggMetadataListe.vedleggListe =
            json.vedlegg.vedlegg
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
        val prosentIkkeLevert: Int,
    )
}

private fun isVedleggskravAnnet(vedlegg: VedleggMetadata) = ANNET == vedlegg.skjema && ANNET == vedlegg.tillegg

@XmlRootElement
data class VedleggMetadataListe(
    var vedleggListe: MutableList<VedleggMetadata> = mutableListOf(),
)

@XmlRootElement
data class VedleggMetadata(
    var filUuid: String? = null,
    var filnavn: String? = null,
    var mimeType: String? = null,
    var filStorrelse: String? = null,
    var status: Vedleggstatus? = null,
    var skjema: String? = null,
    var tillegg: String? = null,
    var hendelseType: JsonVedlegg.HendelseType? = null,
    var hendelseReferanse: String? = null,
)

enum class Vedleggstatus {
    VedleggKreves,
    LastetOpp,
    VedleggAlleredeSendt,
}
