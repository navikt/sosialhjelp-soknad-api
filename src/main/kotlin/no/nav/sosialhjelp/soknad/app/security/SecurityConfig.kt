package no.nav.sosialhjelp.soknad.app.security

import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
            .securityMatcher("/minesaker/innsendte", "/dittnav/pabegynte/aktive", "/soknad/hide/{digisosId}")
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/minesaker/innsendte", "/dittnav/pabegynte/aktive")
                    .hasAnyAuthority(
                        "Level3",
                        "idporten-loa-substantial",
                        "Level4",
                        "idporten-loa-high",
                    )
                    .requestMatchers("/soknad/hide/{digisosId}")
                    .hasAnyAuthority(
                        "Level4",
                        "idporten-loa-high",
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
            .securityMatcher("/admin/unresolved", "/admin/soknad/{soknadId}/resolve")
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2ResourceServer { oauth2 ->
                oauth2.opaqueToken { it.introspector(entraIdIntrospector) }
            }
            .build()

    @Bean
    @Order(3)
    fun defaultFilterChain(
        http: HttpSecurity,
        defaultIntrospector: OpaqueTokenIntrospector,
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
                    .hasAnyAuthority(
                        "Level4",
                        "idporten-loa-high",
                    )
            }.oauth2ResourceServer { oauth2 ->
                oauth2.opaqueToken { it.introspector(defaultIntrospector) }
            }
            .build()

    @Bean
    fun tokenXIntrospector(
        texasService: TexasService,
    ): OpaqueTokenIntrospector = DefaultOpaqueTokenIntrospector(IdentityProvider.TOKENX, texasService)

    @Bean
    fun entraIdIntrospector(
        texasService: TexasService,
    ): OpaqueTokenIntrospector = DefaultOpaqueTokenIntrospector(IdentityProvider.ENTRA_ID, texasService)

    @Bean
    fun defaultIntrospector(
        texasService: TexasService,
    ): OpaqueTokenIntrospector = DefaultOpaqueTokenIntrospector(IdentityProvider.IDPORTEN, texasService)
}
