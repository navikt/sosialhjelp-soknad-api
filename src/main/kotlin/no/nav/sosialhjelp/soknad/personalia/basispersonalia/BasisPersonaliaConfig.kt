package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(BasisPersonaliaRessurs::class)
open class BasisPersonaliaConfig(
    private val personService: PersonService
) {

    @Bean
    open fun basisPersonaliaSystemdata(): BasisPersonaliaSystemdata {
        return BasisPersonaliaSystemdata(personService)
    }
}