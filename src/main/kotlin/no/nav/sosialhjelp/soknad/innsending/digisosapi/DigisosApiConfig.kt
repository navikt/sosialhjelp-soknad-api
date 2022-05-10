package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DigisosApiConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val krypteringService: KrypteringService
) {

    @Bean
    open fun digisosApiV1Client(): DigisosApiV1Client {
        val digisosApiV1Client = DigisosApiV1ClientImpl(
            digisosApiEndpoint,
            integrasjonsidFiks,
            integrasjonpassordFiks,
            kommuneInfoService,
            krypteringService
        )
        return MetricsFactory.createTimerProxy("DigisosApi", digisosApiV1Client, DigisosApiV1Client::class.java)
    }

    @Bean
    open fun digisosApiV2Client(): DigisosApiV2Client {
        val digisosApiV2Client = DigisosApiV2ClientImpl(
            digisosApiEndpoint,
            integrasjonsidFiks,
            integrasjonpassordFiks,
            krypteringService
        )
        return MetricsFactory.createTimerProxy("DigisosApi", digisosApiV2Client, DigisosApiV2Client::class.java)
    }

    @Bean
    open fun digisosApiPing(digisosApiV1Client: DigisosApiV1Client): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(digisosApiEndpoint, "DigisosApi", true)
            try {
                digisosApiV1Client.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }
}
