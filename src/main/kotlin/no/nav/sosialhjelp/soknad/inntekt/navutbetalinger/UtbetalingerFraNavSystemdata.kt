package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class UtbetalingerFraNavSystemdata(
    private val organisasjonService: OrganisasjonService,
    private val navUtbetalingerService: NavUtbetalingerService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return

        val jsonData = jsonInternalSoknad.soknad.data
        val personIdentifikator = jsonData.personalia.personIdentifikator.verdi
        val okonomiOpplysningUtbetalinger = jsonData.okonomi.opplysninger.utbetaling

        fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger)

        jsonInternalSoknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet = false
        val systemUtbetalingerNav = innhentNavSystemregistrertInntekt(personIdentifikator)
//        TODO: fjerne
        log.info("Hentet utbetalinger fra nav: $systemUtbetalingerNav")
        if (systemUtbetalingerNav == null) {
            jsonInternalSoknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet = true
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerNav)
        }
    }

    private fun fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>) {
        okonomiOpplysningUtbetalinger.removeIf {
            it.type.equals(SoknadJsonTyper.UTBETALING_NAVYTELSE, ignoreCase = true)
        }
    }

    private fun innhentNavSystemregistrertInntekt(personIdentifikator: String): List<JsonOkonomiOpplysningUtbetaling>? {
        val utbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager(personIdentifikator) ?: return null
//        TODO: fjern
        log.info("utbetaliner siste 40 dager: $utbetalinger")
        return utbetalinger.map { mapToJsonOkonomiOpplysningUtbetaling(it) }
    }

    fun mapToJsonOrganisasjon(orgnummer: String?): JsonOrganisasjon? {
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

    private fun mapToJsonOkonomiOpplysningUtbetaling(navUtbetaling: NavUtbetaling): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.SYSTEM)
            .withType(SoknadJsonTyper.UTBETALING_NAVYTELSE)
            .withTittel(navUtbetaling.tittel)
            .withBelop(tilIntegerMedAvrunding(navUtbetaling.netto.toString()))
            .withNetto(navUtbetaling.netto)
            .withBrutto(navUtbetaling.brutto)
            .withSkattetrekk(navUtbetaling.skattetrekk)
            .withOrganisasjon(mapToJsonOrganisasjon(navUtbetaling.orgnummer))
            .withAndreTrekk(navUtbetaling.andreTrekk)
            .withPeriodeFom(if (navUtbetaling.periodeFom != null) navUtbetaling.periodeFom.toString() else null)
            .withPeriodeTom(if (navUtbetaling.periodeTom != null) navUtbetaling.periodeTom.toString() else null)
            .withUtbetalingsdato(if (navUtbetaling.utbetalingsdato == null) null else navUtbetaling.utbetalingsdato.toString())
            .withKomponenter(tilUtbetalingskomponentListe(navUtbetaling.komponenter))
            .withOverstyrtAvBruker(false)
    }

    private fun tilUtbetalingskomponentListe(komponenter: List<Komponent>?): List<JsonOkonomiOpplysningUtbetalingKomponent> {
        return komponenter?.map {
            JsonOkonomiOpplysningUtbetalingKomponent()
                .withBelop(it.belop)
                .withType(it.type)
                .withSatsBelop(it.satsBelop)
                .withSatsType(it.satsType)
                .withSatsAntall(it.satsAntall)
        } ?: ArrayList()
    }

    companion object {
        private val log by logger()
        fun tilIntegerMedAvrunding(s: String): Int? {
            val d = tilDouble(s) ?: return null
            return d.roundToInt()
        }

        private fun tilDouble(s: String): Double? {
            if (StringUtils.isBlank(s)) {
                return null
            }
            return StringUtils.deleteWhitespace(s.replace(",", ".").replace("\u00A0", "")).toDouble()
        }
    }
}
