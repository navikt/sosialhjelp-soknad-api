package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg

// Sortering av lister i json-modellen slik at sammenlikning av json-string ikke blir feil
class JsonInternalSoknadListSorter(val original: JsonInternalSoknad, val shadow: JsonInternalSoknad) {
    fun doSorting() {
        original.doSorting()
        shadow.doSorting()
    }
}

private fun JsonInternalSoknad.doSorting() {
    sortAnsvar()
    sortOkonomi()
    sortArbeidsforhold()
    sortVedlegg()
}

private fun JsonInternalSoknad.sortAnsvar() {
    soknad?.data?.familie?.forsorgerplikt?.ansvar?.sortBy { it.barn.fodselsdato }
}

private fun JsonInternalSoknad.sortOkonomi() {
    soknad?.data?.okonomi?.opplysninger?.sortLists()
    soknad?.data?.okonomi?.oversikt?.sortLists()
    soknad?.data?.okonomi?.opplysninger?.sortBostotte()
}

private fun JsonInternalSoknad.sortArbeidsforhold() {
    soknad?.data?.arbeid?.forhold?.sortWith(
        compareBy<JsonArbeidsforhold> { it.arbeidsgivernavn }
            .thenBy { it.fom }
            .thenBy { it.tom }
            .thenBy { it.stillingsprosent }
            .thenBy { it.stillingstype },
    )
}

private fun JsonInternalSoknad.sortVedlegg() {
    vedlegg?.vedlegg?.sortWith(
        compareBy<JsonVedlegg> { it.type }
            .thenBy { it.tilleggsinfo }
            .thenBy { it.hendelseType }
            .thenBy { it.hendelseReferanse },
    )
    vedlegg?.vedlegg?.forEach { it.sortFiler() }
}

private fun JsonVedlegg.sortFiler() {
    filer?.sortWith(compareBy<JsonFiler> { it.filnavn }.thenBy { it.sha512 })
}

private fun JsonOkonomiopplysninger.sortBostotte() {
    bostotte?.saker?.sortWith(
        compareBy<JsonBostotteSak> { it.dato }
            .thenBy { it.type }
            .thenBy { it.status }
            .thenBy { it.vedtaksstatus },
    )
}

private fun JsonOkonomioversikt.sortLists() {
    formue?.sortWith(compareBy<JsonOkonomioversiktFormue> { it.type }.thenBy { it.tittel }.thenBy { it.belop })
    utgift?.sortWith(compareBy<JsonOkonomioversiktUtgift> { it.type }.thenBy { it.tittel }.thenBy { it.belop })
    inntekt?.sortWith(
        compareBy<JsonOkonomioversiktInntekt> { it.type }.thenBy { it.tittel }.thenBy { it.brutto }.thenBy { it.netto },
    )
}

private fun JsonOkonomiopplysninger.sortLists() {
    bekreftelse?.sortWith(compareBy<JsonOkonomibekreftelse> { it.type }.thenBy { it.tittel })
    utgift?.sortWith(compareBy<JsonOkonomiOpplysningUtgift> { it.type }.thenBy { it.tittel }.thenBy { it.belop })
    utbetaling?.sortWith(
        compareBy<JsonOkonomiOpplysningUtbetaling> { it.type }
            .thenBy { it.tittel }
            .thenBy { it.brutto }
            .thenBy { it.netto }
            .thenBy { it.belop }
            .thenBy { it.utbetalingsdato },
    )
}
