package no.nav.sosialhjelp.soknad.api.informasjon.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

enum class LoggLevel {
    ERROR,
    WARN,
    INFO,
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Logg(
    val level: LoggLevel,
    val message: String?,
    val jsFileUrl: String?,
    val lineNumber: String?,
    val columnNumber: String?,
    val url: String?,
    val userAgent: String?,
) {
    fun melding(): String {
        var useragentWithoutSpaceAndComma = ""
        if (userAgent != null) {
            val useragentWithoutSpace = userAgent.replace(" ", "_")
            useragentWithoutSpaceAndComma = useragentWithoutSpace.replace(",", "_")
        }
        return String.format(
            "jsmessagehash=%s, fileUrl=%s:%s:%s, url=%s, userAgent=%s, melding: %s",
            message.hashCode(),
            jsFileUrl,
            lineNumber,
            columnNumber,
            url,
            useragentWithoutSpaceAndComma,
            message,
        )
    }
}
