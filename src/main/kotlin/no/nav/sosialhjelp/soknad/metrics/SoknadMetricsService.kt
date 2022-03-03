package no.nav.sosialhjelp.soknad.metrics

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.isVedleggskravAnnet
import no.nav.sosialhjelp.soknad.metrics.MetricsUtils.getProsent
import org.slf4j.LoggerFactory

class SoknadMetricsService {

    fun reportSendSoknadMetrics(
        soknadUnderArbeid: SoknadUnderArbeid,
        vedleggList: List<VedleggMetadata>
    ) {
        reportSendSoknad(soknadUnderArbeid.erEttersendelse())
        countAndreportVedleggskrav(soknadUnderArbeid.erEttersendelse(), vedleggList)
    }

    fun reportStartSoknad(isEttersendelse: Boolean) {
        reportSoknad("soknad.start", isEttersendelse)
    }

    private fun reportSendSoknad(isEttersendelse: Boolean) {
        reportSoknad("soknad.send", isEttersendelse)
    }

    fun reportAvbruttSoknad(isEttersendelse: Boolean) {
        reportSoknad("soknad.avbrutt", isEttersendelse)
    }

    private fun reportSoknad(name: String, isEttersendelse: Boolean) {
        val soknadstype = (if (isEttersendelse) "ettersending." else "") + SOKNAD_TYPE
        val event = MetricsFactory.createEvent(name)
        event.addFieldToReport("soknadstype", soknadstype)
        event.report()
    }

    fun reportVedleggskrav(
        isEttersendelse: Boolean,
        totaltAntall: Int,
        antallInnsendt: Int,
        antallLevertTidligere: Int,
        antallIkkeLevert: Int
    ) {
        val sendtype = if (isEttersendelse) "ettersendelse" else "soknad"
        val event = MetricsFactory.createEvent("digisos.vedleggskrav")
        event.addTagToReport("sendetype", sendtype)
        event.addFieldToReport("antall.totalt", totaltAntall)
        event.addFieldToReport("antall.innsendt", antallInnsendt)
        event.addFieldToReport("antall.levertTidligere", antallLevertTidligere)
        event.addFieldToReport("antall.ikkeLevert", antallIkkeLevert)
        event.addFieldToReport("prosent.innsendt", getProsent(antallInnsendt, totaltAntall))
        event.addFieldToReport("prosent.levertTidligere", getProsent(antallLevertTidligere, totaltAntall))
        event.addFieldToReport("prosent.ikkeLevert", getProsent(antallIkkeLevert, totaltAntall))
        event.report()
    }

    fun countAndreportVedleggskrav(isEttersendelse: Boolean, vedleggList: List<VedleggMetadata>) {
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
        reportVedleggskrav(isEttersendelse, totaltAntall, antallInnsendt, antallLevertTidligere, antallIkkeLevert)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadMetricsService::class.java)
    }
}
