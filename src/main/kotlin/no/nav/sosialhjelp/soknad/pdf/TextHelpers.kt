package no.nav.sosialhjelp.soknad.pdf

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.BasisPersonaliaSystemdata
import org.springframework.stereotype.Component

@Component
class TextHelpers(
    private val kodeverkService: KodeverkService
) {

    fun fulltNavnForLand(landForkortelse: String?): String? {
        if (landForkortelse == null || landForkortelse == "???" || landForkortelse.equals(
                "YYY",
                ignoreCase = true
            ) || landForkortelse.equals(
                    BasisPersonaliaSystemdata.PDL_UKJENT_STATSBORGERSKAP, ignoreCase = true
                )
        ) {
            return "Vi har ikke opplysninger om ditt statsborgerskap"
        } else if (landForkortelse.equals(
                BasisPersonaliaSystemdata.PDL_STATSLOS,
                ignoreCase = true
            ) || landForkortelse.equals("XXA", ignoreCase = true)
        ) {
            return "Statsløs"
        }
        return kodeverkService.getLand(landForkortelse)
    }
}
