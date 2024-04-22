package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addInntektIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeInntektIfPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiForventningService
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ArbeidsforholdSystemdata(
    private val arbeidsforholdService: ArbeidsforholdService,
    private val textService: TextService,
    private val v2AdapterService: V2AdapterService,
    private val okonomiForventningService: OkonomiForventningService,
) : Systemdata {
    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return
        internalSoknad.soknad.data.arbeid.forhold = innhentSystemArbeidsforhold(soknadUnderArbeid) ?: emptyList()
        updateVedleggForventninger(soknadUnderArbeid.behandlingsId, internalSoknad, textService, okonomiForventningService)
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
            .withStillingstype(
                arbeidsforhold.harFastStilling?.let {
                    when {
                        it -> Stillingstype.FAST
                        else -> Stillingstype.VARIABEL
                    }
                },
            )
            .withOverstyrtAvBruker(java.lang.Boolean.FALSE)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ArbeidsforholdSystemdata::class.java)

        fun updateVedleggForventninger(
            behandlingsId: String,
            internalSoknad: JsonInternalSoknad,
            textService: TextService,
            okonomiForventningService: OkonomiForventningService,
        ) {
            val utbetalinger = internalSoknad.soknad.data.okonomi.opplysninger.utbetaling
            val inntekter = internalSoknad.soknad.data.okonomi.oversikt.inntekt
            val jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(internalSoknad)

            okonomiForventningService.setOppysningUtbetalinger(behandlingsId, utbetalinger, SLUTTOPPGJOER, jsonVedleggs.any { it.type == "sluttoppgjor" })

            if (jsonVedleggs.any { it.type == "lonnslipp" }) {
                val tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[JOBB])
                addInntektIfNotPresentInOversikt(inntekter, JOBB, tittel)
            } else {
                removeInntektIfPresentInOversikt(inntekter, JOBB)
            }
        }
    }
}
