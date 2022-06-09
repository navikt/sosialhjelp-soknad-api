package no.nav.sosialhjelp.soknad.innsending.svarut.client

import no.ks.fiks.svarut.klient.model.Forsendelse
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
open class SvarUtService(
    private val svarUtClient: SvarUtClient
) {

    open fun send(forsendelse: Forsendelse, filnavnInputStreamMap: Map<String, InputStream>): String? {
        val forsendelseId = svarUtClient.sendForsendelse(forsendelse, filnavnInputStreamMap)
        return forsendelseId?.id.toString()
    }
}
