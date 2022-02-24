package no.nav.sosialhjelp.soknad.personalia.person

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.personalia.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class PersonConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    private val redisService: RedisService,
    kodeverkService: KodeverkService
) {

    private val helper: MapperHelper = MapperHelper()
    private val mapper = PdlDtoMapper(kodeverkService, helper)

    @Bean
    open fun personService(hentPersonClient: HentPersonClient): PersonService {
        return PersonService(hentPersonClient, helper, mapper)
    }

    @Bean
    open fun hentPersonClient(): HentPersonClient {
        return HentPersonClientImpl(client, baseurl, redisService)
    }

    private val client: Client
        get() = RestUtils.createClient()
}
