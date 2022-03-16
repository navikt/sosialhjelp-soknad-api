package no.nav.sosialhjelp.soknad.common.oidc

import no.nav.security.token.support.core.configuration.IssuerProperties
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter
import no.nav.sosialhjelp.soknad.common.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.common.Constants.TOKENX
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.net.MalformedURLException
import java.net.URL

@Configuration
@Import(OidcResourceFilteringFeature::class)
open class OidcTokenValidatorConfig(
    @Value("\${oidc.issuer.selvbetjening.cookie_name}") private val cookieName: String,
    @Value("\${oidc.issuer.selvbetjening.accepted_audience}") private val acceptedAudience: String,
    @Value("\${oidc.issuer.selvbetjening.discoveryurl}") private val discoveryUrl: String,
    @Value("\${oidc.issuer.selvbetjening.proxy_url}") private val proxyUrl: String,
    @Value("\${oidc.issuer.tokenx.accepted_audience}") private val acceptedAudienceTokenx: String,
    @Value("\${oidc.issuer.tokenx.discoveryurl}") private val discoveryUrlTokenx: String
) {

    @Bean
    open fun jaxrsJwtTokenValidationFilter(multiIssuerConfiguration: MultiIssuerConfiguration?): JaxrsJwtTokenValidationFilter {
        return JaxrsJwtTokenValidationFilter(multiIssuerConfiguration)
    }

    @Bean
    open fun multiIssuerConfiguration(resourceRetriever: ProxyAwareResourceRetriever?): MultiIssuerConfiguration {
        return MultiIssuerConfiguration(issuerPropertiesMap, resourceRetriever)
    }

    @Bean
    open fun proxyAwareResourceRetriever(): ProxyAwareResourceRetriever {
        return ProxyAwareResourceRetriever()
    }

    private val issuerPropertiesMap: Map<String, IssuerProperties>
        get() {
            val issuerPropertiesMap: MutableMap<String, IssuerProperties> = HashMap()
            issuerPropertiesMap[SELVBETJENING] = selvbetjeningProperties()
            issuerPropertiesMap[TOKENX] = tokenxProperties()
            return issuerPropertiesMap
        }

    private fun selvbetjeningProperties(): IssuerProperties {
        val issuerProperties = IssuerProperties(toUrl(discoveryUrl), listOf(acceptedAudience), cookieName)
        issuerProperties.proxyUrl = proxyUrl(proxyUrl)
        return issuerProperties
    }

    private fun tokenxProperties(): IssuerProperties {
        return IssuerProperties(toUrl(discoveryUrlTokenx), listOf(acceptedAudienceTokenx))
    }

    private fun toUrl(url: String?): URL {
        return try {
            URL(url)
        } catch (e: MalformedURLException) {
            throw IllegalStateException("Kunne ikke parse property til en URL", e)
        }
    }

    private fun proxyUrl(proxyUrl: String?, issuer: String = SELVBETJENING): URL? {
        return try {
            URL(proxyUrl)
        } catch (e: MalformedURLException) {
            log.info("Kunne ikke parse property 'oidc.issuer.$issuer.proxy_url' til en URL. Fortsetter uten proxy.")
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OidcTokenValidatorConfig::class.java)
    }
}
