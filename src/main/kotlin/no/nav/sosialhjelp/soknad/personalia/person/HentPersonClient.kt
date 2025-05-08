package no.nav.sosialhjelp.soknad.personalia.person

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
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
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
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl), HentPersonClient {
    override fun hentPerson(ident: String): PersonDto? = doPdlRequest(ident, HENT_PERSON, "hentPerson")

    override fun hentAdressebeskyttelse(ident: String): PersonAdressebeskyttelseDto? =
        doPdlRequest(ident, HENT_ADRESSEBESKYTTELSE, "adressebeskyttelse")

    private fun <T> doPdlRequest(
        ident: String,
        query: String,
        typeRequest: String,
    ): T? =
        runCatching {
            doRequest(PdlRequest(query, variables(ident)), typeRequest)
                .let { response -> parse<HentPersonDto<T>>(response).also { it.checkForPdlApiErrors() }.data.hentPerson }
        }
            .getOrElse {
                when (it) {
                    is PdlApiException -> throw it
                    else -> throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", it)
                }
            }

    private fun doRequest(
        pdlRequest: PdlRequest,
        typeRequest: String,
    ): String =
        hentPersonRequest
            .header(AUTHORIZATION, BEARER + tokenX)
            .bodyValue(pdlRequest)
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(pdlRetry)
            .block() ?: throw PdlApiException("Noe feilet mot PDL - $typeRequest - response null?")

    @Deprecated("Skal ikke hente informasjon om ektefelle uten samtykke")
    override fun hentEktefelle(ident: String): EktefelleDto? =
        try {
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
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            logger.error("Kall til PDL feilet (hentEktefelle)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }

    @Deprecated("Skal ikke hente informasjon om ektefelle uten samtykke")
    override fun hentBarn(ident: String): BarnDto? =
        try {
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
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            logger.error("Kall til PDL feilet (hentBarn)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }

    private val tokenX get() = texasService.exchangeToken(IdentityProvider.TOKENX, target = pdlAudience)

    @Deprecated("Skal ikke benytte system-token for uthenting av persondata")
    private fun azureAdToken() = texasService.getToken(IdentityProvider.AZURE_AD, pdlScope)

    private fun variables(ident: String): Map<String, Any> = mapOf("historikk" to false, "ident" to ident)

    private val hentPersonRequest get() = baseRequest.header(HEADER_TEMA, TEMA_KOM)

    companion object {
        private val logger = getLogger(HentPersonClient::class.java)
    }
}
