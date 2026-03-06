package no.nav.sosialhjelp.soknad.app.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks

@Configuration
class ReactorConfig {
    @PostConstruct
    fun enableAutomaticContextPropagation() {
        // Enables automatic propagation of ThreadLocal values (including MDC) into Reactor Context
        // This is required because we use WebClient (reactive) without full WebFlux on the classpath
        // The spring.reactor.context-propagation property only works when WebFlux is present
        Hooks.enableAutomaticContextPropagation()
    }
}
