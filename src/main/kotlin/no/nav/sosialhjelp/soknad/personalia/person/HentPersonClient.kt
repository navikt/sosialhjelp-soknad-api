package no.nav.sosialhjelp.soknad.personalia.person

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.client.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.pdl.HentPersonDto
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.HENT_ADRESSEBESKYTTELSE
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.HENT_BARN
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.HENT_EKTEFELLE
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.HENT_PERSON
import no.nav.sosialhjelp.soknad.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.client.redis.ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.BARN_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.EKTEFELLE_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.PDL_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.PERSON_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.common.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.ProcessingException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.Client
import javax.ws.rs.core.HttpHeaders.AUTHORIZATION

interface HentPersonClient {
    fun hentPerson(ident: String): PersonDto?
    fun hentEktefelle(ident: String): EktefelleDto?
    fun hentBarn(ident: String): BarnDto?
    fun hentAdressebeskyttelse(ident: String): PersonAdressebeskyttelseDto?
}

@Component
class HentPersonClientImpl(
    client: Client = RestUtils.createClient(),
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    private val azureadService: AzureadService,
) : PdlClient(client, baseurl), HentPersonClient {

    override fun hentPerson(ident: String): PersonDto? {
        return hentPersonFraCache(ident) ?: hentPersonFraPdl(ident)
    }

    private fun hentPersonFraCache(ident: String): PersonDto? {
        return redisService.get(PERSON_CACHE_KEY_PREFIX + ident, PersonDto::class.java) as? PersonDto
    }

    private fun hentPersonFraPdl(ident: String): PersonDto? {
        return try {
            val response: String = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(WebApplicationException::class, ProcessingException::class)
                ) {
                    hentPersonRequest
                        .header(AUTHORIZATION, BEARER + tokenXtoken(ident))
                        .post(requestEntity(HENT_PERSON, variables(ident)), String::class.java)
                }
            }
            val pdlResponse = parse<HentPersonDto<PersonDto>>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data.hentPerson
                ?.also { lagreTilCache(PERSON_CACHE_KEY_PREFIX, ident, it) }
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentPerson)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    override fun hentEktefelle(ident: String): EktefelleDto? {
        return hentEktefelleFraCache(ident) ?: hentEktefelleFraPdl(ident)
    }

    private fun hentEktefelleFraCache(ident: String): EktefelleDto? {
        return redisService.get(EKTEFELLE_CACHE_KEY_PREFIX + ident, EktefelleDto::class.java) as EktefelleDto?
    }

    private fun hentEktefelleFraPdl(ident: String): EktefelleDto? {
        return try {
            val response: String = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(WebApplicationException::class, ProcessingException::class)
                ) {
                    hentPersonRequest
                        .header(AUTHORIZATION, BEARER + azureadService.getSystemToken(pdlScope))
                        .post(requestEntity(HENT_EKTEFELLE, variables(ident)), String::class.java)
                }
            }
            val pdlResponse = parse<HentPersonDto<EktefelleDto>>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data.hentPerson
                ?.also { lagreTilCache(EKTEFELLE_CACHE_KEY_PREFIX, ident, it) }
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentEktefelle)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    override fun hentBarn(ident: String): BarnDto? {
        return hentBarnFraCache(ident) ?: hentBarnFraPdl(ident)
    }

    private fun hentBarnFraCache(ident: String): BarnDto? {
        return redisService.get(BARN_CACHE_KEY_PREFIX + ident, BarnDto::class.java) as? BarnDto
    }

    private fun hentBarnFraPdl(ident: String): BarnDto? {
        return try {
            val response: String = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(WebApplicationException::class, ProcessingException::class)
                ) {
                    hentPersonRequest
                        .header(AUTHORIZATION, BEARER + azureadService.getSystemToken(pdlScope))
                        .post(requestEntity(HENT_BARN, variables(ident)), String::class.java)
                }
            }
            val pdlResponse = parse<HentPersonDto<BarnDto>>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data.hentPerson
                ?.also { lagreTilCache(BARN_CACHE_KEY_PREFIX, ident, it) }
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentBarn)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    override fun hentAdressebeskyttelse(ident: String): PersonAdressebeskyttelseDto? {
        return hentAdressebeskyttelseFraCache(ident) ?: hentAdressebeskyttelseFraPdl(ident)
    }

    private fun hentAdressebeskyttelseFraCache(ident: String): PersonAdressebeskyttelseDto? {
        return redisService.get(
            ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX + ident,
            PersonAdressebeskyttelseDto::class.java
        ) as? PersonAdressebeskyttelseDto
    }

    private fun hentAdressebeskyttelseFraPdl(ident: String): PersonAdressebeskyttelseDto? {
        return try {
            val body = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(WebApplicationException::class, ProcessingException::class)
                ) {
                    hentPersonRequest
                        .header(AUTHORIZATION, BEARER + tokenXtoken(ident))
                        .post(requestEntity(HENT_ADRESSEBESKYTTELSE, variables(ident)), String::class.java)
                }
            }

            val pdlResponse = parse<HentPersonDto<PersonAdressebeskyttelseDto>>(body)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data.hentPerson
                ?.also { lagreTilCache(ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX, ident, it) }
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentPersonAdressebeskyttelse)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private suspend fun tokenXtoken(ident: String) = tokendingsService.exchangeToken(ident, getToken(), pdlAudience)

    private fun variables(ident: String): Map<String, Any> {
        return mapOf("historikk" to false, "ident" to ident)
    }

    private val hentPersonRequest get() = baseRequest.header(HEADER_TEMA, TEMA_KOM)

    private fun lagreTilCache(prefix: String, ident: String, pdlResponse: Any) {
        try {
            redisService.setex(prefix + ident, pdlMapper.writeValueAsBytes(pdlResponse), PDL_CACHE_SECONDS)
        } catch (e: JsonProcessingException) {
            log.error("Noe feilet ved serialisering av response fra Pdl - {}", pdlResponse.javaClass.name, e)
        }
    }

    companion object {
        private val log = getLogger(HentPersonClient::class.java)
    }
}
