package no.nav.sosialhjelp.soknad.innsending.svarut.client

import no.ks.fiks.svarut.klient.model.Forsendelse
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SvarUtService(
    private val svarUtClient: SvarUtClient
) {

    fun send(forsendelse: Forsendelse, filnavnInputStreamMap: Map<String, InputStream>): String? {
        val forsendelseId = svarUtClient.sendForsendelse(forsendelse, filnavnInputStreamMap)
        return forsendelseId?.id.toString()
    }
}
