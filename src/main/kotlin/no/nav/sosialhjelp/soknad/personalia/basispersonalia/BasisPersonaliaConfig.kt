package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class BasisPersonaliaConfig(
    private val personService: PersonService
) {

    @Bean
    open fun basisPersonaliaSystemdata(): BasisPersonaliaSystemdata {
        return BasisPersonaliaSystemdata(personService)
    }
}
