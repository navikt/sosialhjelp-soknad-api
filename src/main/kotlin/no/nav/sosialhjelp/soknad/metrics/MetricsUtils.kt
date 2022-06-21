package no.nav.sosialhjelp.soknad.metrics

import kotlin.math.roundToInt

object MetricsUtils {
    fun navKontorTilInfluxNavn(mottaker: String?): String {
        return mottaker?.replace("NAV", "")?.replace(",", "") ?: ""
    }

    fun getProsent(partial: Int, total: Int): Int {
        if (total == 0) return 0
        val prosent = partial.toDouble() / total * 100 // Cast til double for å få desimaler i delingen
        return prosent.roundToInt()
    }
}
