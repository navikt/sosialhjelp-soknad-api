package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet

object MigrationToolkit {
    /**
     * Oppretter en JsonOkonomibeskrivelserAvAnnet hvis den ikke finnes, og setter verdiene gitt som parametre.
     *
     * Gadd vite hvorfor dataen er strukturert slik, men beskrivelseAvAnnet er altså et felles objekt som inneholder teksten som er oppgitt
     * alle de forskjellige stedene i søknaden hvor man kan huke av for "annet". Flere steder i legacy-koden brukes lignende metoder.
     *
     * I respons til en kommentar som uttrykket berettiget forvirring tenkte jeg å gjøre det litt tydeligere hva denne datastrukturen er ved å sentralisere behandlingen her.
     */
    fun JsonOkonomiopplysninger.updateOrCreateBeskrivelseAvAnnet(
        verdi: String? = null,
        sparing: String? = null,
        utbetaling: String? = null,
        boutgifter: String? = null,
        barneutgifter: String? = null,
    ) = this.beskrivelseAvAnnet?.let {
        it.verdi = verdi ?: it.verdi
        it.sparing = sparing ?: it.sparing
        it.utbetaling = utbetaling ?: it.utbetaling
        it.boutgifter = boutgifter ?: it.boutgifter
        it.barneutgifter = barneutgifter ?: it.barneutgifter
    } ?: JsonOkonomibeskrivelserAvAnnet()
        .withKilde(JsonKildeBruker.BRUKER)
        .withVerdi(verdi ?: "")
        .withSparing(sparing ?: "")
        .withUtbetaling(utbetaling ?: "")
        .withBoutgifter(boutgifter ?: "")
        .withBarneutgifter(barneutgifter ?: "")

    fun JsonOkonomiopplysninger.updateBekreftelse(
        type: String,
        verdi: Boolean?,
        tittel: String? = null,
    ) {
        this.bekreftelse = this.bekreftelse.orEmpty().toMutableList()
        val bekreftelse = this.bekreftelse.firstOrNull { it.type == type }

        if (verdi == null) {
            this.bekreftelse.removeIf { it.type == type }
        } else if (bekreftelse == null) {
            this.bekreftelse.add(JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(type).withTittel(tittel).withVerdi(verdi))
        } else {
            bekreftelse.verdi = verdi
        }
    }

    fun JsonOkonomiopplysninger.getBekreftelseVerdi(type: String): Boolean? = this.bekreftelse.firstOrNull { it.type == type }?.verdi

    fun JsonOkonomiopplysninger.getSamtykkeDato(type: String): String? = this.bekreftelse.sortedByDescending { it.bekreftelsesDato }.firstOrNull { it.type == type && it.verdi }?.bekreftelsesDato

    fun JsonOkonomiopplysninger.hasUtgift(jsonSoknadType: String): Boolean = this.utgift.any { it.type == jsonSoknadType }

    fun JsonOkonomiopplysninger.hasUtbetaling(jsonSoknadType: String): Boolean = this.utbetaling.any { it.type == jsonSoknadType }

    fun JsonOkonomioversikt.hasUtgift(jsonSoknadType: String): Boolean = this.utgift.any { it.type == jsonSoknadType }

    fun JsonOkonomioversikt.hasFormue(jsonSoknadType: String): Boolean = this.formue.any { it.type == jsonSoknadType }
}
