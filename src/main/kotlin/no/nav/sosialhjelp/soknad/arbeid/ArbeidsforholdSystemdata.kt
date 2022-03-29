package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addInntektIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.removeInntektIfPresentInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.common.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ArbeidsforholdSystemdata(
    private val arbeidsforholdService: ArbeidsforholdService,
    private val textService: TextService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val eier = soknadUnderArbeid.eier
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return
        internalSoknad.soknad.data.arbeid.forhold = innhentSystemArbeidsforhold(eier)
        updateVedleggForventninger(internalSoknad, textService)
    }

    fun innhentSystemArbeidsforhold(personIdentifikator: String): List<JsonArbeidsforhold>? {
        val arbeidsforholds: List<Arbeidsforhold>? = try {
            arbeidsforholdService.hentArbeidsforhold(personIdentifikator)
        } catch (e: Exception) {
            LOG.warn("Kunne ikke hente arbeidsforhold", e)
            null
        }
        return arbeidsforholds?.map { mapToJsonArbeidsforhold(it) }
    }

    private fun mapToJsonArbeidsforhold(arbeidsforhold: Arbeidsforhold): JsonArbeidsforhold {
        return JsonArbeidsforhold()
            .withArbeidsgivernavn(arbeidsforhold.arbeidsgivernavn)
            .withFom(arbeidsforhold.fom)
            .withTom(arbeidsforhold.tom)
            .withKilde(JsonKilde.SYSTEM)
            .withStillingsprosent(arbeidsforhold.fastStillingsprosent?.let { Math.toIntExact(it) })
            .withStillingstype(arbeidsforhold.harFastStilling?.let { tilJsonStillingstype(it) })
            .withOverstyrtAvBruker(java.lang.Boolean.FALSE)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ArbeidsforholdSystemdata::class.java)
        fun updateVedleggForventninger(internalSoknad: JsonInternalSoknad, textService: TextService) {
            val utbetalinger = internalSoknad.soknad.data.okonomi.opplysninger.utbetaling
            val inntekter = internalSoknad.soknad.data.okonomi.oversikt.inntekt
            val jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(internalSoknad)
            if (typeIsInList(jsonVedleggs, "sluttoppgjor")) {
                val tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[SLUTTOPPGJOER])
                addUtbetalingIfNotPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER, tittel)
            } else {
                removeUtbetalingIfPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER)
            }
            if (typeIsInList(jsonVedleggs, "lonnslipp")) {
                val tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[JOBB])
                addInntektIfNotPresentInOversikt(inntekter, JOBB, tittel)
            } else {
                removeInntektIfPresentInOversikt(inntekter, JOBB)
            }
        }

        private fun typeIsInList(jsonVedleggs: List<JsonVedlegg>, vedleggstype: String): Boolean {
            return jsonVedleggs.any { it.type == vedleggstype }
        }

        private fun tilJsonStillingstype(harFastStilling: Boolean): Stillingstype {
            return if (harFastStilling) Stillingstype.FAST else Stillingstype.VARIABEL
        }
    }
}
