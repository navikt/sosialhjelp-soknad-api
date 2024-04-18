package no.nav.sosialhjelp.soknad.personalia.person

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.app.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.app.client.pdl.HentPersonDto
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_ADRESSEBESKYTTELSE
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_BARN
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_EKTEFELLE
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_PERSON
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.redis.ADRESSEBESKYTTELSE_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.BARN_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.EKTEFELLE_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.PDL_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.redis.PERSON_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

interface HentPersonClient {
    fun hentPerson(ident: String): PersonDto?

    fun hentEktefelle(ident: String): EktefelleDto?

    fun hentBarn(ident: String): BarnDto?

    fun hentAdressebeskyttelse(ident: String): PersonAdressebeskyttelseDto?
}

@Component
class HentPersonClientImpl(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    private val azureadService: AzureadService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl), HentPersonClient {
    override fun hentPerson(ident: String): PersonDto? {
        return hentPersonFraCache(ident) ?: hentPersonFraPdl(ident)
    }

    private fun hentPersonFraCache(ident: String): PersonDto? {
        return redisService.get(PERSON_CACHE_KEY_PREFIX + ident, PersonDto::class.java) as? PersonDto
    }

    private fun hentPersonFraPdl(ident: String): PersonDto? {
        return try {
            val response =
                hentPersonRequest
                    .header(AUTHORIZATION, BEARER + tokenXtoken(ident))
                    .bodyValue(PdlRequest(HENT_PERSON, variables(ident)))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - hentPerson - response null?")
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
            val response =
                hentPersonRequest
                    .header(AUTHORIZATION, BEARER + azureAdToken())
                    .bodyValue(PdlRequest(HENT_EKTEFELLE, variables(ident)))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - hentEktefelle - response null?")
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
            val response: String =
                hentPersonRequest
                    .header(AUTHORIZATION, BEARER + azureAdToken())
                    .bodyValue(PdlRequest(HENT_BARN, variables(ident)))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - hentBarn - response null?")
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
            PersonAdressebeskyttelseDto::class.java,
        ) as? PersonAdressebeskyttelseDto
    }

    private fun hentAdressebeskyttelseFraPdl(ident: String): PersonAdressebeskyttelseDto? {
        return try {
            val body: String =
                hentPersonRequest
                    .header(AUTHORIZATION, BEARER + tokenXtoken(ident))
                    .bodyValue(PdlRequest(HENT_ADRESSEBESKYTTELSE, variables(ident)))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - hentAdressebeskyttelse - response null?")
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

    private fun tokenXtoken(ident: String) =
        runBlocking {
            tokendingsService.exchangeToken(ident, getToken(), pdlAudience)
        }

    private fun azureAdToken() =
        runBlocking {
            azureadService.getSystemToken(pdlScope)
        }

    private fun variables(ident: String): Map<String, Any> {
        return mapOf("historikk" to false, "ident" to ident)
    }

    private val hentPersonRequest get() = baseRequest.header(HEADER_TEMA, TEMA_KOM)

    private fun lagreTilCache(
        prefix: String,
        ident: String,
        pdlResponse: Any,
    ) {
        try {
            redisService.setex(prefix + ident, pdlMapper.writeValueAsBytes(pdlResponse), PDL_CACHE_SECONDS)
        } catch (e: JsonProcessingException) {
            log.error("Noe feilet ved serialisering av response fra Pdl - ${pdlResponse.javaClass.name}", e)
        }
    }

    companion object {
        private val log = getLogger(HentPersonClient::class.java)
    }
}
