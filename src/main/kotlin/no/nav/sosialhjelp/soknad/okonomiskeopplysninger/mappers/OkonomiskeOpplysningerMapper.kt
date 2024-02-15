package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addUtgiftIfNotPresentInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeUtgiftIfPresentInOpplysninger
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggRadFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import org.apache.commons.lang3.StringUtils.isEmpty

object OkonomiskeOpplysningerMapper {

    fun addAllInntekterToJsonOkonomi(
        rader: List<VedleggRadFrontend>,
        oversikt: JsonOkonomioversikt,
        soknadType: String?
    ) {
        val inntekt = oversikt.inntekt.firstOrNull { it.type == soknadType } ?: throw IkkeFunnetException("inntekt $soknadType finnes ikke i søknad")
        oversikt.inntekt = oversikt.inntekt.filter { it.type != soknadType }.plus(rader.map { mapToInntekt(it, inntekt.type, inntekt.tittel) })
    }

    fun addAllInntekterToJsonOkonomiUtbetalinger(
        rader: List<VedleggRadFrontend>,
        opplysninger: JsonOkonomiopplysninger,
        soknadType: String
    ) {
        val inntekt = opplysninger.utbetaling.firstOrNull { it.type == soknadType } ?: throw IkkeFunnetException("inntekt $soknadType finnes ikke i søknad")
        opplysninger.utbetaling =
            opplysninger.utbetaling.filter { it.type != soknadType }.plus(rader.map { mapToUtbetaling(it, inntekt.type, inntekt.tittel, false) })
    }

    fun addAllFormuerToJsonOkonomi(
        rader: List<VedleggRadFrontend>,
        oversikt: JsonOkonomioversikt,
        soknadType: String?
    ) {
        val formue = oversikt.formue.firstOrNull { it.type == soknadType } ?: throw IkkeFunnetException("formue $soknadType finnes ikke i søknad")
        oversikt.formue = oversikt.formue.filter { it.type != soknadType }.plus(rader.map { mapToFormue(it, formue.type, formue.tittel) })
    }

    fun addAllOversiktUtgifterToJsonOkonomi(
        rader: List<VedleggRadFrontend>,
        oversikt: JsonOkonomioversikt,
        soknadType: String?
    ) {
        val oversiktUtgift = oversikt.utgift.firstOrNull { it.type == soknadType }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Har du flere tabber oppe samtidig?")

        oversikt.utgift = oversikt.utgift
            .filter { it.type != soknadType }
            .plus(rader.map { mapToOversiktUtgift(it, oversiktUtgift) })

        // Spesialtilfelle for boliglan. Må kjøre på nytt for å få med renter
        if (soknadType == UTGIFTER_BOLIGLAN_AVDRAG) {
            val renter =
                oversikt.utgift.firstOrNull { it.type == UTGIFTER_BOLIGLAN_RENTER } ?: throw IkkeFunnetException("renter for boliglån finnes ikke i søknaden")
            oversikt.utgift = oversikt.utgift.filter { it.type == UTGIFTER_BOLIGLAN_RENTER }.plus(rader.map { mapToOversiktUtgift(it, renter) })
        }
    }

    fun addAllOpplysningUtgifterToJsonOkonomi(
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        soknadType: String?
    ) {
        var eksisterendeOpplysningUtgift = jsonOkonomi.opplysninger.utgift
            .firstOrNull { it.type == soknadType }

        if (vedleggFrontend.type == VedleggType.AnnetAnnet) {
            eksisterendeOpplysningUtgift = JsonOkonomiOpplysningUtgift()
                .withType(SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER)
                .withTittel("Annen (brukerangitt): ")
            val utgifter = jsonOkonomi.opplysninger.utgift
            if (checkIfTypeAnnetAnnetShouldBeRemoved(vedleggFrontend)) {
                removeUtgiftIfPresentInOpplysninger(utgifter, soknadType)
                return
            } else {
                addUtgiftIfNotPresentInOpplysninger(utgifter, soknadType, eksisterendeOpplysningUtgift.tittel)
            }
        }

        eksisterendeOpplysningUtgift
            ?.let { utgift ->
                val utgifter = jsonOkonomi.opplysninger.utgift
                    .filter { it.type != soknadType }
                    .toMutableList()
                utgifter.addAll(mapToOppysningUtgiftList(vedleggFrontend.rader, utgift))
                jsonOkonomi.opplysninger.utgift = utgifter
            }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Har du flere tabber oppe samtidig?")
    }

    private fun checkIfTypeAnnetAnnetShouldBeRemoved(vedleggFrontend: VedleggFrontend): Boolean {
        return vedleggFrontend.rader?.size == 1 &&
            vedleggFrontend.rader[0].belop == null &&
            isEmpty(vedleggFrontend.rader[0].beskrivelse)
    }

    fun addAllUtbetalingerToJsonOkonomi(
        rader: List<VedleggRadFrontend>,
        opplysninger: JsonOkonomiopplysninger,
        soknadType: String?
    ) {
        val eksisterendeUtbetaling =
            opplysninger.utbetaling.firstOrNull { it.type == soknadType } ?: throw IkkeFunnetException("Utbetaling $soknadType eksisterer ikke i søknad")

        opplysninger.utbetaling = opplysninger.utbetaling
            .filter { it.type != soknadType }
            .plus(rader.map { mapToUtbetaling(it, eksisterendeUtbetaling.type, eksisterendeUtbetaling.tittel, true) })
    }

    private fun mapToInntekt(
        rad: VedleggRadFrontend,
        type: String?,
        tittel: String?,
    ): JsonOkonomioversiktInntekt = JsonOkonomioversiktInntekt()
        .withKilde(JsonKilde.BRUKER)
        .withType(type)
        .withTittel(tittel)
        .withBrutto(rad.brutto ?: rad.belop)
        .withNetto(rad.netto ?: rad.belop)
        .withOverstyrtAvBruker(false)

    private fun mapToUtbetaling(
        rad: VedleggRadFrontend,
        type: String?,
        tittel: String?,
        brukBelop: Boolean
    ): JsonOkonomiOpplysningUtbetaling {
        val jsonOkonomiOpplysningUtbetaling = JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.BRUKER)
            .withType(type)
            .withTittel(tittel)
            .withOverstyrtAvBruker(false)
        if (brukBelop) {
            jsonOkonomiOpplysningUtbetaling.withBelop(rad.belop)
        } else {
            jsonOkonomiOpplysningUtbetaling.withNetto(rad.belop?.toDouble())
        }
        return jsonOkonomiOpplysningUtbetaling
    }

    private fun mapToFormue(
        radFrontend: VedleggRadFrontend,
        type: String?,
        tittel: String?,
    ): JsonOkonomioversiktFormue = JsonOkonomioversiktFormue()
        .withKilde(JsonKilde.BRUKER)
        .withType(type)
        .withTittel(tittel)
        .withBelop(radFrontend.belop)
        .withOverstyrtAvBruker(false)

    private fun mapToOversiktUtgift(
        radFrontend: VedleggRadFrontend,
        eksisterendeUtgift: JsonOkonomioversiktUtgift
    ): JsonOkonomioversiktUtgift {
        val tittel = eksisterendeUtgift.tittel
        val typetittel = getTypetittel(tittel)
        val type = eksisterendeUtgift.type
        return JsonOkonomioversiktUtgift()
            .withKilde(JsonKilde.BRUKER)
            .withType(type)
            .withTittel(getTittelWithBeskrivelse(typetittel, radFrontend.beskrivelse))
            .withBelop(if (type == UTGIFTER_BOLIGLAN_AVDRAG) radFrontend.avdrag else if (type == UTGIFTER_BOLIGLAN_RENTER) radFrontend.renter else radFrontend.belop)
            .withOverstyrtAvBruker(false)
    }

    private fun getTittelWithBeskrivelse(typetittel: String?, beskrivelse: String?): String? =
        if (beskrivelse != null) typetittel + beskrivelse else typetittel

    private fun getTypetittel(tittel: String?): String? = when {
        tittel == null -> null
        !tittel.contains(":") -> tittel
        else -> tittel.substring(0, tittel.indexOf(":") + 1) + " "
    }

    private fun mapToOppysningUtgiftList(
        rader: List<VedleggRadFrontend>?,
        eksisterendeUtgift: JsonOkonomiOpplysningUtgift
    ): List<JsonOkonomiOpplysningUtgift> {
        return rader?.map { mapToOppysningUtgift(it, eksisterendeUtgift) } ?: emptyList()
    }

    private fun mapToOppysningUtgift(
        radFrontend: VedleggRadFrontend,
        eksisterendeUtgift: JsonOkonomiOpplysningUtgift
    ): JsonOkonomiOpplysningUtgift {
        val tittel = eksisterendeUtgift.tittel
        val typetittel = getTypetittel(tittel)
        val type = eksisterendeUtgift.type
        return JsonOkonomiOpplysningUtgift()
            .withKilde(JsonKilde.BRUKER)
            .withType(type)
            .withTittel(getTittelWithBeskrivelse(typetittel, radFrontend.beskrivelse))
            .withBelop(radFrontend.belop)
            .withOverstyrtAvBruker(false)
    }
}
