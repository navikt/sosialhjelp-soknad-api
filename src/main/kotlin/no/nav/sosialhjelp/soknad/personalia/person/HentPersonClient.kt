package no.nav.sosialhjelp.soknad.personalia.person

import kotlinx.coroutines.reactor.awaitSingleOrNull
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.pdl.HentPersonDto
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_ADRESSEBESKYTTELSE
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_BARN
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_EKTEFELLE
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_PERSON
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.NonBlockingTexasService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

interface HentPersonClient {
    suspend fun hentPerson(
        personId: String,
        userToken: String,
    ): PersonDto?

    suspend fun hentAdressebeskyttelse(): PersonAdressebeskyttelseDto?

    suspend fun hentEktefelle(ektefelleIdent: String): EktefelleDto?

    suspend fun hentBarn(barnIdent: String): BarnDto?
}

@Component
class HentPersonClientImpl(
    @param:Value("\${pdl_api_url}") private val baseurl: String,
    @param:Value("\${pdl_api_scope}") private val pdlScope: String,
    @param:Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val texasService: NonBlockingTexasService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl), HentPersonClient {
    // må caches på dette nivået da den kalles 2 steder i PersonService
    @Cacheable(HentPersonClientConfig.CACHE_NAME, key = "#personId", unless = "#result == null")
    override suspend fun hentPerson(
        personId: String,
        userToken: String,
    ): PersonDto? =
        doPdlRequest(PdlRequest(HENT_PERSON, variables(personId)), "hentPerson", userToken)

    override suspend fun hentAdressebeskyttelse(): PersonAdressebeskyttelseDto? =
        doPdlRequest(PdlRequest(HENT_ADRESSEBESKYTTELSE, variables(currentUserContext().userId)), "adressebeskyttelse", currentUserContext().userToken)

    override suspend fun hentEktefelle(ektefelleIdent: String): EktefelleDto? =
        doPdlRequest(PdlRequest(HENT_EKTEFELLE, variables(ektefelleIdent)), "hentEktefelle", azureAdToken())

    override suspend fun hentBarn(barnIdent: String): BarnDto? =
        doPdlRequest(PdlRequest(HENT_BARN, variables(barnIdent)), "hentBarn", azureAdToken())

    private suspend inline fun <reified T> doPdlRequest(
        pdlRequest: PdlRequest,
        typeRequest: String,
        userToken: String,
    ): T? =
        runCatching {
            doRequest(pdlRequest, userToken) ?: throw PdlApiException("Noe feilet mot PDL - $typeRequest - response null?")
        }
            .getOrElse {
                when (it) {
                    is PdlApiException -> throw it
                    else -> throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", it)
                }
            }
            .let { response -> parseResponse(response) }

    private inline fun <reified T> parseResponse(response: String): T? =
        parse<HentPersonDto<T>>(response)
            .also { it.checkForPdlApiErrors() }
            .data.hentPerson

    private suspend fun doRequest(
        pdlRequest: PdlRequest,
        userToken: String,
    ): String? =
        hentPersonRequest
            .header(AUTHORIZATION, BEARER + getTokenX(userToken))
            .bodyValue(pdlRequest)
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
            .timeout(Duration.ofSeconds(10))
            .awaitSingleOrNull()

    private suspend fun getTokenX(userToken: String) = texasService.exchangeToken(userToken, IdentityProvider.TOKENX, target = pdlAudience)

    private suspend fun azureAdToken() = texasService.getToken(IdentityProvider.AZURE_AD, pdlScope)

    private fun variables(ident: String): Map<String, Any> = mapOf("historikk" to false, "ident" to ident)

    private val hentPersonRequest get() = baseRequest.header(HEADER_TEMA, TEMA_KOM)

    companion object {
        private const val TEMA_KOM = "KOM"
        private const val HEADER_TEMA = "Tema"
    }
}

@Configuration
class HentPersonClientConfig : SoknadApiCacheConfig(CACHE_NAME, TTL) {
    override fun getConfig(): RedisCacheConfiguration {
        return super
            .getConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    JacksonJsonRedisSerializer(jacksonObjectMapper(), PersonDto::class.java),
                ),
            )
    }

    companion object {
        const val CACHE_NAME = "hentPersonCache"
        private val TTL = Duration.ofMinutes(10)
    }
}
