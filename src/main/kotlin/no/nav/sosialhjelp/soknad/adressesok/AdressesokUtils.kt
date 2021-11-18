package no.nav.sosialhjelp.soknad.adressesok

import org.apache.commons.text.WordUtils
import java.util.Locale

fun formatterKommunenavn(kommunenavn: String?): String? {
    return kommunenavn?.lowercase(Locale.getDefault())?.split(" ")?.toTypedArray()
        ?.map { if (it != "og") WordUtils.capitalize(it, '-') else it }
        ?.joinToString(" ") { it }
}
