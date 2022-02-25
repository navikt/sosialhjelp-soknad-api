package no.nav.sosialhjelp.soknad.personalia.person

import no.nav.sosialhjelp.soknad.client.azure.AzureadService
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
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
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    private val azureadService: AzureadService,
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
        return HentPersonClientImpl(client, baseurl, pdlScope, pdlAudience, redisService, tokendingsService, azureadService)
    }

    private val client: Client
        get() = RestUtils.createClient()
}
