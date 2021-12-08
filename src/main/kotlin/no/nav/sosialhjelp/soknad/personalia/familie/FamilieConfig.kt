package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FamilieConfig(
    private val personService: PersonService
) {

    @Bean
    open fun familieSystemdata(): FamilieSystemdata {
        return FamilieSystemdata(personService)
    }
}
