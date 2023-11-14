package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType.*
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Komponent
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.generell.toJsonKilde
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type.toSoknadJsonType
import org.springframework.stereotype.Component
import java.util.*

@Component
class InntektMapper(
    private val inntektRepository: InntektRepository
): DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, json: JsonInternalSoknad) {
        json.createChildrenIfNotExists()
        with (json.soknad.data.okonomi) {
            inntektRepository.findAllBySoknadId(soknadId)
                .forEach { mapFromInntekt(it) }
        }
    }

    private fun JsonOkonomi.mapFromInntekt(inntekt: Inntekt) {
        when (inntekt.type) {
            DOKUMENTASJON_FORSIKRINGSUTBETALING,
            DOKUMENTASJON_ANNET_INNTEKTER,
            DOKUMENTASJON_UTBYTTE,
            HUSBANKEN_VEDTAK,
            SALGSOPPGJOR_EIENDOM,
            SLUTTOPPGJOR_ARBEID,
            -> { opplysninger.utbetaling.add(inntekt.toJsonOkonomiOpplysningUtbetaling()) }

            BARNEBIDRAG_MOTTAR,
            LONNSLIPP_ARBEID,
            STUDENT_VEDTAK
            -> { oversikt.inntekt.add(inntekt.toJsonOkonomioversiktInntekt()) }
        }
    }

    private fun Inntekt.toJsonOkonomioversiktInntekt(): JsonOkonomioversiktInntekt {
        return JsonOkonomioversiktInntekt()
            .withKilde(JsonKilde.BRUKER) // alltid bruker
            .withType(type.toSoknadJsonType())
            .withTittel(tittel)
            .withBrutto(brutto)
            .withNetto(netto)
    }

    private fun Inntekt.toJsonOkonomiOpplysningUtbetaling(): JsonOkonomiOpplysningUtbetaling {
        if (this.utbetaling == null) throw IllegalArgumentException("Utbetaling er null for Inntekt med Utbetaling-type $type")
        return JsonOkonomiOpplysningUtbetaling()
            .withKilde(utbetaling.kilde.toJsonKilde())
            .withType(type.toSoknadJsonType())
            .withTittel(tittel)
            .withOrganisasjon(
                JsonOrganisasjon()
                    .withNavn("")
                    .withOrganisasjonsnummer(utbetaling.orgnummer)
            )
            .withBelop(utbetaling.belop)
            .withNetto(netto?.toDouble())
            .withBrutto(brutto?.toDouble())
            .withSkattetrekk(utbetaling.skattetrekk)
            .withAndreTrekk(utbetaling.andreTrekk)
            .withUtbetalingsdato(utbetaling.utbetalingsdato.toString())
            .withPeriodeFom(utbetaling.periodeStart.toString())
            .withPeriodeTom(utbetaling.periodeSlutt.toString())
            .withKomponenter(
                utbetaling.komponent?.map { it.toJsonOkonomiOpplysningUtbetalingKomponent() }
            )
    }

    private fun Komponent.toJsonOkonomiOpplysningUtbetalingKomponent(): JsonOkonomiOpplysningUtbetalingKomponent {
        return JsonOkonomiOpplysningUtbetalingKomponent()
            .withType(type)
            .withBelop(belop)
            .withSatsType(satsType)
            .withSatsAntall(satsAntall)
            .withSatsBelop(satsBelop)
    }
}
