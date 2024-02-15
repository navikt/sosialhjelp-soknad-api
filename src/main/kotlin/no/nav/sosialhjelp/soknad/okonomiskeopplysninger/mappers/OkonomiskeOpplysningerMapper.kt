package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
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
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        soknadType: String?
    ) {
        jsonOkonomi.oversikt.inntekt
            .firstOrNull { it.type == soknadType }
            ?.let { inntekt ->
                val inntekter = jsonOkonomi.oversikt.inntekt
                    .filter { it.type != soknadType }
                    .toMutableList()
                inntekter.addAll(vedleggFrontend.rader?.map { mapToInntekt(it, inntekt) } ?: emptyList())
                jsonOkonomi.oversikt.inntekt = inntekter
            }
            ?: throw IkkeFunnetException("Disse opplysningene tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")
    }

    fun addAllInntekterToJsonOkonomiUtbetalinger(
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        soknadType: String
    ) {
        jsonOkonomi.opplysninger.utbetaling
            .firstOrNull { it.type == soknadType }
            ?.let { inntekt ->
                val inntekter = jsonOkonomi.opplysninger.utbetaling
                    .filter { it.type != soknadType }
                    .toMutableList()
                inntekter.addAll(mapToUtbetalingList(vedleggFrontend.rader, inntekt, false))
                jsonOkonomi.opplysninger.utbetaling = inntekter
            }
            ?: throw IkkeFunnetException("Disse opplysningene tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")
    }

    fun addAllFormuerToJsonOkonomi(
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        soknadType: String?
    ) {
        jsonOkonomi.oversikt.formue
            .firstOrNull { it.type == soknadType }
            ?.let { formue ->
                val formuer = jsonOkonomi.oversikt.formue
                    .filter { it.type != soknadType }
                    .toMutableList()
                formuer.addAll(mapToFormueList(vedleggFrontend.rader, formue))
                jsonOkonomi.oversikt.formue = formuer
            }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Har du flere tabber oppe samtidig?")
    }

    fun addAllOversiktUtgifterToJsonOkonomi(
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        soknadType: String?
    ) {
        jsonOkonomi.oversikt.utgift
            .firstOrNull { it.type == soknadType }
            ?.let { oversiktUtgift ->
                val utgifter = jsonOkonomi.oversikt.utgift
                    .filter { it.type != soknadType }
                    .toMutableList()
                utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, oversiktUtgift))

                // ---------- Spesialtilfelle for boliglan. Må kjøre på nytt for å få med renter ----------
                if (soknadType == UTGIFTER_BOLIGLAN_AVDRAG) {
                    addBoliglanRenterToUtgifter(vedleggFrontend, jsonOkonomi, utgifter)
                }
                // ----------------------------------------------------------------------------------------
                jsonOkonomi.oversikt.utgift = utgifter
            }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Har du flere tabber oppe samtidig?")
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
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        soknadType: String?
    ) {
        jsonOkonomi.opplysninger.utbetaling
            .firstOrNull { it.type == soknadType }
            ?.let { eksisterendeUtbetaling ->
                val utbetalinger = jsonOkonomi.opplysninger.utbetaling
                    .filter { it.type != soknadType }
                    .toMutableList()
                utbetalinger.addAll(mapToUtbetalingList(vedleggFrontend.rader, eksisterendeUtbetaling, true))
                jsonOkonomi.opplysninger.utbetaling = utbetalinger
            }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Har du flere tabber oppe samtidig?")
    }

    private fun addBoliglanRenterToUtgifter(
        vedleggFrontend: VedleggFrontend,
        jsonOkonomi: JsonOkonomi,
        utgifter: MutableList<JsonOkonomioversiktUtgift>
    ) {
        val soknadType = UTGIFTER_BOLIGLAN_RENTER
        jsonOkonomi.oversikt.utgift
            .firstOrNull { it.type == soknadType }
            ?.let { renter ->
                utgifter.removeAll(utgifter.filter { it.type == soknadType })
                utgifter.addAll(mapToOversiktUtgiftList(vedleggFrontend.rader, renter))
            }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $soknadType utgift som har blitt tatt bort fra søknaden. Har du flere tabber oppe samtidig?")
    }

    private fun mapToInntekt(
        rad: VedleggRadFrontend,
        eksisterendeInntekt: JsonOkonomioversiktInntekt
    ): JsonOkonomioversiktInntekt {
        return JsonOkonomioversiktInntekt()
            .withKilde(JsonKilde.BRUKER)
            .withType(eksisterendeInntekt.type)
            .withTittel(eksisterendeInntekt.tittel)
            .withBrutto(if (rad.brutto != null) rad.brutto else rad.belop)
            .withNetto(if (rad.netto != null) rad.netto else rad.belop)
            .withOverstyrtAvBruker(false)
    }

    private fun mapToUtbetalingList(
        rader: List<VedleggRadFrontend>?,
        eksisterendeUtbetaling: JsonOkonomiOpplysningUtbetaling,
        brukBelop: Boolean
    ): List<JsonOkonomiOpplysningUtbetaling> {
        return rader?.map { mapToUtbetaling(it, eksisterendeUtbetaling, brukBelop) } ?: emptyList()
    }

    private fun mapToUtbetaling(
        rad: VedleggRadFrontend,
        eksisterendeUtbetaling: JsonOkonomiOpplysningUtbetaling,
        brukBelop: Boolean
    ): JsonOkonomiOpplysningUtbetaling {
        val jsonOkonomiOpplysningUtbetaling = JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.BRUKER)
            .withType(eksisterendeUtbetaling.type)
            .withTittel(eksisterendeUtbetaling.tittel)
            .withOverstyrtAvBruker(false)
        if (brukBelop) {
            jsonOkonomiOpplysningUtbetaling.withBelop(rad.belop)
        } else {
            jsonOkonomiOpplysningUtbetaling.withNetto(rad.belop?.toDouble())
        }
        return jsonOkonomiOpplysningUtbetaling
    }

    private fun mapToFormueList(
        rader: List<VedleggRadFrontend>?,
        eksisterendeFormue: JsonOkonomioversiktFormue
    ): List<JsonOkonomioversiktFormue> {
        return rader?.map { mapToFormue(it, eksisterendeFormue) } ?: emptyList()
    }

    private fun mapToFormue(
        radFrontend: VedleggRadFrontend,
        eksisterendeFormue: JsonOkonomioversiktFormue
    ): JsonOkonomioversiktFormue {
        return JsonOkonomioversiktFormue()
            .withKilde(JsonKilde.BRUKER)
            .withType(eksisterendeFormue.type)
            .withTittel(eksisterendeFormue.tittel)
            .withBelop(radFrontend.belop)
            .withOverstyrtAvBruker(false)
    }

    private fun mapToOversiktUtgiftList(
        rader: List<VedleggRadFrontend>?,
        eksisterendeUtgift: JsonOkonomioversiktUtgift
    ): List<JsonOkonomioversiktUtgift> {
        return rader?.map { mapToOversiktUtgift(it, eksisterendeUtgift) } ?: emptyList()
    }

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

    private fun getTittelWithBeskrivelse(typetittel: String, beskrivelse: String?): String {
        return if (beskrivelse != null) typetittel + beskrivelse else typetittel
    }

    private fun getTypetittel(tittel: String): String {
        return if (!tittel.contains(":")) tittel else tittel.substring(0, tittel.indexOf(":") + 1) + " "
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
