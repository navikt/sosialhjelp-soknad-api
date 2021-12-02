package no.nav.sosialhjelp.soknad.inntekt.husbanken.enums

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling

enum class BostotteMottaker(val value: String) {
    KOMMUNE(JsonOkonomiOpplysningUtbetaling.Mottaker.KOMMUNE.value()),
    HUSSTAND(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND.value());
}
