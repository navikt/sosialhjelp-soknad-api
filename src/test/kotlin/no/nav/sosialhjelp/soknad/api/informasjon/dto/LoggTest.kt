package no.nav.sosialhjelp.soknad.api.informasjon.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LoggTest {
    @Test
    fun testMeldingOutput() {
        val feilmelding = "Cannot read blabla of undefined"
        val logg = Logg(
            level = LoggLevel.ERROR,
            message = feilmelding,
            jsFileUrl = "minFil.js",
            lineNumber = "100",
            columnNumber = "99",
            url = "http://nav.no/url",
            userAgent = "IE ROCKS,MSIE"
        )
        assertThat(logg.melding()).isEqualTo("jsmessagehash=" + feilmelding.hashCode() + ", fileUrl=minFil.js:100:99, url=http://nav.no/url, userAgent=IE_ROCKS_MSIE, melding: Cannot read blabla of undefined")
    }
}
