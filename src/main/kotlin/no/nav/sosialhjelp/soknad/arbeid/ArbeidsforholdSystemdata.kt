package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addInntektIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeInntektIfPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.springframework.stereotype.Component

@Deprecated("Brukes ikke til formålet lenger. Se RegisterDataFetcher")
@Component
class ArbeidsforholdSystemdata() {
    companion object {
        // TODO Rydd opp / implementer logikk
        fun updateVedleggForventninger(
            internalSoknad: JsonInternalSoknad,
            textService: TextService,
        ) {
            if (internalSoknad.soknad.data.soknadstype == JsonData.Soknadstype.KORT) {
                return
            }
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

        private fun typeIsInList(
            jsonVedleggs: List<JsonVedlegg>,
            vedleggstype: String,
        ): Boolean = jsonVedleggs.any { it.type == vedleggstype }
    }
}
