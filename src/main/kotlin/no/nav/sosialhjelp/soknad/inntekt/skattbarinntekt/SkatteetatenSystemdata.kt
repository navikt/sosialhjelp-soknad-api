package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdSystemdata
import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.slf4j.LoggerFactory

class SkatteetatenSystemdata(
    private val skattbarInntektService: SkattbarInntektService,
    private val organisasjonService: OrganisasjonService,
    private val textService: TextService
) {

    fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val jsonData = soknadUnderArbeid.jsonInternalSoknad.soknad.data
        val personIdentifikator = jsonData.personalia.personIdentifikator.verdi
        val okonomiOpplysningUtbetalinger = jsonData.okonomi.opplysninger.utbetaling
        val bekreftelser = jsonData.okonomi.opplysninger.bekreftelse

        if (bekreftelser.any { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) && it.verdi }) {
            val systemUtbetalingerSkattbar = innhentSkattbarSystemregistrertInntekt(personIdentifikator)
            if (systemUtbetalingerSkattbar == null) {
                soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = true
            } else {
                bekreftelser
                    .firstOrNull { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) }
                    ?.withBekreftelsesDato(SoknadUnderArbeidService.nowWithForcedNanoseconds())
                fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger)
                okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerSkattbar)
                soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = false
            }
        } else { // Ikke samtykke!!!
            fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger)
            soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = false
        }
        // Dette kan påvirke hvilke forventinger vi har til arbeidsforhold:
        ArbeidsforholdSystemdata.updateVedleggForventninger(soknadUnderArbeid.jsonInternalSoknad, textService)
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

    private fun mapToJsonOrganisasjon(orgnummer: String?): JsonOrganisasjon? {
        if (orgnummer == null) return null
        if (orgnummer.matches(Regex("\\d{9}"))) {
            return JsonOrganisasjon()
                .withNavn(organisasjonService.hentOrgNavn(orgnummer))
                .withOrganisasjonsnummer(orgnummer)
        }
        if (orgnummer.matches(Regex("\\d{11}"))) {
            log.info("Utbetalingens opplysningspliktigId er et personnummer. Dette blir ikke inkludert i soknad.json")
        } else {
            log.error("Utbetalingens opplysningspliktigId er verken et organisasjonsnummer eller personnummer: $orgnummer. Kontakt skatteetaten.")
        }
        return null
    }

    private fun mapToJsonOkonomiOpplysningUtbetaling(utbetaling: Utbetaling): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.SYSTEM)
            .withType(SoknadJsonTyper.UTBETALING_SKATTEETATEN)
            .withTittel(utbetaling.tittel)
            .withBrutto(utbetaling.brutto)
            .withSkattetrekk(utbetaling.skattetrekk)
            .withOrganisasjon(mapToJsonOrganisasjon(utbetaling.orgnummer))
            .withPeriodeFom(if (utbetaling.periodeFom != null) utbetaling.periodeFom.toString() else null)
            .withPeriodeTom(if (utbetaling.periodeTom != null) utbetaling.periodeTom.toString() else null)
            .withOverstyrtAvBruker(false)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SkatteetatenSystemdata::class.java)
    }
}
