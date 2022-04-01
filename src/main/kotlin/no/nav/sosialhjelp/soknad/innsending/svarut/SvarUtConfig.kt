package no.nav.sosialhjelp.soknad.innsending.svarut

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SvarUtConfig(
    @Value("\${svarut_url}") private var baseurl: String,
    private val svarUtClient: SvarUtClient
) {

    @Bean
    open fun svarUtPing(): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "SvarUt", false)
            try {
                svarUtClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }
}
