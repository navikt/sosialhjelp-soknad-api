package no.nav.sosialhjelp.soknad.tekster

import java.util.Locale

open class TextService(
    private val navMessageSource: NavMessageSource
) {
    open fun getJsonOkonomiTittel(key: String?): String? {
        val properties = navMessageSource.getBundleFor("sendsoknad", Locale("nb", "NO"))
        return properties.getProperty("json.okonomi.$key")
    }
}
