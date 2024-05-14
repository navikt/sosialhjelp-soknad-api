package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class UtbetalingerFraNavSystemdata(
    private val organisasjonService: OrganisasjonService,
    private val navUtbetalingerService: NavUtbetalingerService,
    private val v2AdapterService: V2AdapterService,
) : Systemdata {
    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad ?: return

        val jsonData = jsonInternalSoknad.soknad.data
        val personIdentifikator = jsonData.personalia.personIdentifikator.verdi
        val okonomiOpplysningUtbetalinger = jsonData.okonomi.opplysninger.utbetaling

        fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger)

        jsonInternalSoknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet = false
        val systemUtbetalingerNav = innhentNavSystemregistrertInntekt(personIdentifikator)
        log.info("${systemUtbetalingerNav?.size} utbetalinger fra Nav og legges til i økonomiopplysninger")
        if (systemUtbetalingerNav == null) {
            jsonInternalSoknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet = true
            v2AdapterService.setUtbetalingFraNav(soknadUnderArbeid.behandlingsId, true)
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
        return utbetalinger.map { mapToJsonOkonomiOpplysningUtbetaling(it) }
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
            .withOrganisasjon(organisasjonService.mapToJsonOrganisasjon(navUtbetaling.orgnummer))
            .withAndreTrekk(navUtbetaling.andreTrekk)
            .withPeriodeFom(navUtbetaling.periodeFom?.toString())
            .withPeriodeTom(navUtbetaling.periodeTom?.toString())
            .withUtbetalingsdato(navUtbetaling.utbetalingsdato?.toString())
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
