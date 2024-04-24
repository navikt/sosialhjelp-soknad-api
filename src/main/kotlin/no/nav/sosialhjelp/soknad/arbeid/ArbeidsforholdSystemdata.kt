package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setInntektInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtbetalingInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ArbeidsforholdSystemdata(
    private val arbeidsforholdService: ArbeidsforholdService,
    private val textService: TextService,
    private val v2AdapterService: V2AdapterService,
) : Systemdata {
    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return
        internalSoknad.soknad.data.arbeid.forhold = innhentSystemArbeidsforhold(soknadUnderArbeid) ?: emptyList()
        updateVedleggForventninger(internalSoknad, textService)
    }

    private fun innhentSystemArbeidsforhold(soknadUnderArbeid: SoknadUnderArbeid): List<JsonArbeidsforhold>? {
        val arbeidsforholds: List<Arbeidsforhold>? =
            try {
                arbeidsforholdService.hentArbeidsforhold(soknadUnderArbeid.eier)
            } catch (e: Exception) {
                LOG.warn("Kunne ikke hente arbeidsforhold", e)
                null
            }
        // NyModell
        v2AdapterService.addArbeidsforholdList(soknadUnderArbeid.behandlingsId, arbeidsforholds)
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

        fun updateVedleggForventninger(
            internalSoknad: JsonInternalSoknad,
            textService: TextService,
        ) {
            val utbetalinger = internalSoknad.soknad.data.okonomi.opplysninger.utbetaling
            val inntekter = internalSoknad.soknad.data.okonomi.oversikt.inntekt
            val jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(internalSoknad)
            setUtbetalingInOpplysninger(utbetalinger, SLUTTOPPGJOER, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[SLUTTOPPGJOER]), typeIsInList(jsonVedleggs, "sluttoppgjor"))
            setInntektInOversikt(inntekter, JOBB, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[JOBB]), typeIsInList(jsonVedleggs, "lonnslipp"))
        }

        private fun typeIsInList(
            jsonVedleggs: List<JsonVedlegg>,
            vedleggstype: String,
        ): Boolean {
            return jsonVedleggs.any { it.type == vedleggstype }
        }

        private fun tilJsonStillingstype(harFastStilling: Boolean): Stillingstype {
            return if (harFastStilling) Stillingstype.FAST else Stillingstype.VARIABEL
        }
    }
}
