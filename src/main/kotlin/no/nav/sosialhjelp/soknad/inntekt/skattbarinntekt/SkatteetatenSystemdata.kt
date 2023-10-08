package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdSystemdata
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService.Companion.nowWithForcedNanoseconds
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.springframework.stereotype.Component

@Component
class SkatteetatenSystemdata(
    private val skattbarInntektService: SkattbarInntektService,
    private val organisasjonService: OrganisasjonService,
    private val textService: TextService
) {

    fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return

        val jsonData = jsonInternalSoknad.soknad.data
        val personIdentifikator = jsonData.personalia.personIdentifikator.verdi
        val okonomiOpplysningUtbetalinger = jsonData.okonomi.opplysninger.utbetaling
        val bekreftelser = jsonData.okonomi.opplysninger.bekreftelse

        if (bekreftelser.any { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) && it.verdi }) {
            val systemUtbetalingerSkattbar = innhentSkattbarSystemregistrertInntekt(personIdentifikator)
            if (systemUtbetalingerSkattbar == null) {
                jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = true
            } else {
                bekreftelser
                    .firstOrNull { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) }
                    ?.withBekreftelsesDato(nowWithForcedNanoseconds())
                fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger)
                okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerSkattbar)
                jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = false
            }
        } else { // Ikke samtykke!
            fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger)
            jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = false
        }
        // Dette kan påvirke hvilke forventinger vi har til arbeidsforhold:
        ArbeidsforholdSystemdata.updateVedleggForventninger(jsonInternalSoknad, textService)
    }

    private fun fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>) {
        okonomiOpplysningUtbetalinger.removeIf {
            it.type.equals(SoknadJsonTyper.UTBETALING_SKATTEETATEN, ignoreCase = true)
        }
    }

    private fun innhentSkattbarSystemregistrertInntekt(personIdentifikator: String): List<JsonOkonomiOpplysningUtbetaling>? {
        val utbetalinger = skattbarInntektService.hentUtbetalinger(personIdentifikator) ?: return null
        return utbetalinger.map { mapToJsonOkonomiOpplysningUtbetaling(it) }
    }

    private fun mapToJsonOkonomiOpplysningUtbetaling(utbetaling: Utbetaling): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.SYSTEM)
            .withType(SoknadJsonTyper.UTBETALING_SKATTEETATEN)
            .withTittel(utbetaling.tittel)
            .withBrutto(utbetaling.brutto)
            .withSkattetrekk(utbetaling.skattetrekk)
            .withOrganisasjon(organisasjonService.mapToJsonOrganisasjon(utbetaling.orgnummer))
            .withPeriodeFom(utbetaling.periodeFom.toString())
            .withPeriodeTom(utbetaling.periodeTom.toString())
            .withOverstyrtAvBruker(false)
    }
}
