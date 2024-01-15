package no.nav.sosialhjelp.soknad.pdf

import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaSystemdata.Companion.PDL_STATSLOS
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaSystemdata.Companion.PDL_UKJENT_STATSBORGERSKAP
import org.springframework.stereotype.Component

@Component
class TextHelpers(
    private val kodeverkService: KodeverkService,
) {

    fun fulltNavnForLand(landForkortelse: String?): String? {
        if (landForkortelse == null || landForkortelse.equals(PDL_UKJENT_STATSBORGERSKAP, ignoreCase = true)) {
            return "Vi har ikke opplysninger om ditt statsborgerskap"
        } else if (landForkortelse.equals(PDL_STATSLOS, ignoreCase = true)) {
            return "Statsløs"
        }
        return kodeverkService.getLand(landForkortelse)
    }
}
