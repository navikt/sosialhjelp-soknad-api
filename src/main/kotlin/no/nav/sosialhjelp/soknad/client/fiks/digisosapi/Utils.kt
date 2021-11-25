package no.nav.sosialhjelp.soknad.client.fiks.digisosapi

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import java.util.regex.Pattern

object Utils {
    val digisosObjectMapper = JsonSosialhjelpObjectMapper
        .createObjectMapper()
        .registerKotlinModule()

    fun getDigisosIdFromResponse(errorResponse: String?, behandlingsId: String?): String? {
        if (errorResponse != null && errorResponse.contains(behandlingsId!!) && errorResponse.contains("finnes allerede")) {
            val p = Pattern.compile("^.*?message.*([0-9a-fA-F]{8}[-]?(?:[0-9a-fA-F]{4}[-]?){3}[0-9a-fA-F]{12}).*?$")
            val m = p.matcher(errorResponse)
            if (m.matches()) {
                return m.group(1)
            }
        }
        return null
    }
}
