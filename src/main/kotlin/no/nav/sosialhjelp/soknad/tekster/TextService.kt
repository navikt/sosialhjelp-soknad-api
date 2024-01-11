package no.nav.sosialhjelp.soknad.tekster

import org.springframework.stereotype.Component
import java.util.Locale

@Component
class TextService(
    private val navMessageSource: NavMessageSource
) {
    fun getJsonOkonomiTittel(key: String?): String {
        val properties = navMessageSource.getBundleFor("sendsoknad", Locale.forLanguageTag("nb-NO"))
        return properties.getProperty("json.okonomi.$key")
    }
}
