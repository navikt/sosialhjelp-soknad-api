package no.nav.sosialhjelp.soknad.app.security

import no.nav.sosialhjelp.soknad.api.dittnav.DittNavMetadataRessurs.Companion.DITTNAV_PATH
import no.nav.sosialhjelp.soknad.api.feiledesoknader.AdminController.Companion.ADMIN_BASE_PATH
import no.nav.sosialhjelp.soknad.api.feiledesoknader.AdminController.Companion.RESOLVE_PATH
import no.nav.sosialhjelp.soknad.api.feiledesoknader.AdminController.Companion.UNRESOLVED_PATH
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerMetadataRessurs.Companion.INNSENDTE_PATH
import no.nav.sosialhjelp.soknad.api.minesaker.MineSakerMetadataRessurs.Companion.MINESAKER_BASE_PATH
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LOA_HIGH
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LOA_SUBSTANTIAL
import no.nav.sosialhjelp.soknad.app.security.principal.CitizenPrincipal
import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@Configuration
class SecurityConfig {
    @Bean
    @Order(1)
    fun tokenXFilterChain(
        http: HttpSecurity,
        tokenXIntrospector: OpaqueTokenIntrospector,
    ): SecurityFilterChain =
        http
            .securityMatcher(
                MINESAKER_BASE_PATH + INNSENDTE_PATH,
                DITTNAV_PATH,
            )
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        MINESAKER_BASE_PATH + INNSENDTE_PATH,
                        DITTNAV_PATH,
                    )
                    .hasAnyAuthority(
                        CLAIM_ACR_LEVEL_3,
                        CLAIM_ACR_LOA_SUBSTANTIAL,
                        CLAIM_ACR_LEVEL_4,
                        CLAIM_ACR_LOA_HIGH,
                    )
                    .anyRequest().denyAll()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.opaqueToken { it.introspector(tokenXIntrospector) }
            }
            .build()

    @Bean
    @Order(2)
    fun entraIdFilterChain(
        http: HttpSecurity,
        entraIdIntrospector: OpaqueTokenIntrospector,
    ): SecurityFilterChain =
        http
            .securityMatcher(
                ADMIN_BASE_PATH + UNRESOLVED_PATH,
                ADMIN_BASE_PATH + RESOLVE_PATH,
            )
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2ResourceServer { oauth2 -> oauth2.opaqueToken { it.introspector(entraIdIntrospector) } }
            .build()

    @Bean
    @Order(3)
    fun defaultFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/v3/api-docs/**", "/internal/**", "/minesaker/ping")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt(jwtCustomizer())
            }
            .build()

    private fun jwtCustomizer(): Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer> {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        val authenticationConverter =
            JwtAuthenticationConverter().apply {
                setJwtPrincipalConverter { jwt ->
                    CitizenPrincipal(jwt.tokenValue, jwt.claims, authoritiesConverter.convert(jwt))
                }
            }
        return Customizer { it.jwtAuthenticationConverter(authenticationConverter) }
    }

    @Bean
    fun tokenXIntrospector(
        texasService: TexasService,
    ): OpaqueTokenIntrospector = DefaultOpaqueTokenIntrospector(IdentityProvider.TOKENX, texasService)

    @Bean
    fun entraIdIntrospector(
        texasService: TexasService,
    ): OpaqueTokenIntrospector = DefaultOpaqueTokenIntrospector(IdentityProvider.ENTRA_ID, texasService)
}
